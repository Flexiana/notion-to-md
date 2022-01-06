(ns notion-to-md.core
  (:gen-class)
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [environ.core :refer [env]]
    [notion-to-md.http-client :as http-client])
  (:import
    (java.io
      File)))

(def docs-path "docs/readme/") ; must end with /
(def fetch-children (atom (fn [])))

(declare ->md)
(declare element->md)

(def heading-chars
  {:heading_1 "#"
   :heading_2 "##"
   :heading_3 "###"})

(def no-annotations
  {:bold false
   :italic false
   :strikethrough false
   :code false})

(def md-chars
  {:bold "**"
   :italic "*"
   :strikethrough "~~"
   :code "`"})

(defn make-heading [text]
  {:object "block"
   :has_children false
   :type "heading_1"
   :heading_1
   {:text
    [{:type "text"
      :text {:content text :link nil}
      :annotations no-annotations
      :plain_text text
      :href nil}]}})

(defn sort-md-chars
  "They must be sorted this way to show fine at github"
  [coll]
  (sort-by
    key
    (fn [k1 k2]
      (if (= k1 k2)
        0
        (case [k1 k2]
          [:code :strikethrough] -1
          [:code :italic] -1
          [:code :bold] -1
          [:italic :code] 1
          [:italic :strikethrough] 1
          [:italic :bold] 1
          [:bold :italic] -1
          [:bold :strikethrough] 1
          [:bold :code] 1
          [:strikethrough :italic] -1
          [:strikethrough :code] 1
          [:strikethrough :bold] -1
          (compare k1 k2))))
    coll))

(defn spaces-prefix
  "Example: 
  input '  abc' 
  output '  '"
  [text]
  (apply str
         (take-while (fn [c] (= c \space)) text)))

(defn spaces-suffix
  "Example: 
  input 'abc  ' 
  output '  '"
  [text]
  (->> text
       reverse
       spaces-prefix))

(defn ->spaces-affixes
  [text]
  {:prefix (spaces-prefix text)
   :suffix (spaces-suffix text)})

(defn apply-space-affixes
  "Example: 
  raw-text ' abc '  processed-text '~~abc~~'
  output ' ~~abc~~ '"
  [raw-text processed-text]
  (let [{:keys [prefix suffix]} (->spaces-affixes raw-text)]
    (str prefix processed-text suffix)))

(defn- surround
  "Example: 
  text 'abc'  surrounding '*'
  output '*abc*'"
  [text surrounding]
  (str surrounding text surrounding))

(defn apply-annotations
  "Example: 
  input: 'abc' marked as bold 
  output '*abc*'
  annotations: {
                 bold: false,
                 italic: false,
                 strikethrough: false,
                 underline: false, <-- unsupported
                 code: false,
                 color: default <-- unsupported
                 }"
  [{:keys [annotations]
    :or {annotations no-annotations}
    :as text-element}]
  (let [text (or (:plain_text text-element)
                 (get-in text-element [:equation :expression])
                 (get-in text-element [:text :content]))
        surroundings (sort-md-chars
                       (merge-with (fn [b c] (if b c ""))
                                   (->> md-chars
                                        keys
                                        (select-keys annotations))
                                   md-chars))
        text-with-marks (reduce (fn [acc item]
                                  (surround acc item))
                                (str/trim text)
                                (vals surroundings))]
    (if (str/blank? (str/trim text))
      text
      (apply-space-affixes text text-with-marks))))

(defn code->md
  [prefix text language]
  (str "```" language "\n"
       (let [spaces (apply str
                           (take (inc (count prefix))
                                 (iterate str  "  ")))]
         (->> text
              str/split-lines
              (map (fn [line] (str spaces line "\n")))
              (apply str)))
       "\n"
       prefix "```\n"))

(defn parse-text
  "input: a https://developers.notion.com/reference/rich-text
   output: if its a link, [text] (link), else, the text with Markdown surroundings"
  [text-element]
  (if-let [link (or (:href text-element)
                    (get-in text-element [:text :link :url]))]
    (str "[" (apply-annotations text-element) "](" link ")")
    (apply-annotations text-element)))

(defn parse-image!
  "Creates a file and returns a file link markdown formatted
   It assumes it's a png file"
  [{:keys [image]}]
  (let [url (-> image :file :url)
        caption (-> image :caption first)
        file-name (str (or (get-in caption [:text :content])
                           (get-in caption [:plain_text])
                           (.toString (java.util.UUID/randomUUID)))
                       ".png")]
    (io/copy (http-client/fetch-image url)
             (File. (str docs-path file-name)))
    (str
      "![" file-name "](" docs-path file-name ")")))

(defn fetch-notion-children [id]
  (->> id
       (@fetch-children)
       :results))

(defn parse-paragraph
  "Paragraph blocks
  Paragraph block objects contain the following information within the paragraph property:
  Property	Type	Description
  text	array of rich text objects	Rich text in the paragraph block.
  children	array of block objects	Any nested children blocks of the paragraph block."
  [{:keys [has_children id paragraph]}]
  (let [{:keys [text]} paragraph
        texts (map parse-text text)]
    (if has_children
      (str (reduce str texts) "\n"
           "\n" (element->md (fetch-notion-children id)))
      (str (reduce str texts) "\n"))))

(defn placeholder-parser
  "Parser not supported or not implemented.
  If there is a text, show it, else dump the element form"
  [element]
  (if-let [text-coll (:text element)]
    (reduce str (map parse-text text-coll))
    (str "\n?" element "?\n")))

(defn parse-heading
  "See https://developers.notion.com/reference/block heading blocks"
  [heading-key]
  (fn [block]
    (let [{:keys [text]} (heading-key block)]
      (str "\n" (heading-key heading-chars) " " (reduce str (map parse-text text)) "\n"))))

(defn parse-code
  "returns ```language\ncode;\n```"
  [prefix]
  (fn [{:keys [code]}]
    (let [text (reduce str (map parse-text (:text code)))]
      (code->md prefix text (:language code)))))

(defn parse-callout
  "An emoji plus a text"
  [prefix]
  (fn [{:keys [has_children id callout]}]
    (let [text (reduce str (map parse-text (:text callout)))
          content (if-let [emoji (->> callout :icon :emoji)]
                    (str emoji "  " text "\n")
                    (str text "\n"))]
      (if has_children
        (str "- " content
             (element->md (fetch-notion-children id)
                          prefix))
        (str "- " content)))))

(defn parse-quote
  "https://www.markdownguide.org/basic-syntax/#blockquotes-1"
  [prefix]
  (fn [{:keys [id has_children quote]}]
    (let [text (reduce str (map parse-text (:text quote)))]
      (if has_children
        (str "> " text "\n"
             (element->md (fetch-notion-children id)
                          prefix))
        (str "> " text "\n")))))

(defn parse-bulleted-list-item
  "https://www.markdownguide.org/basic-syntax/#unordered-lists"
  [prefix]
  (fn [{:keys [has_children id bulleted_list_item]}]
    (let [text (reduce str (map parse-text (:text bulleted_list_item)))]
      (if has_children
        (str "- " text "\n\n"
             (element->md (fetch-notion-children id)
                          prefix))
        (str "- " text "\n")))))

(defn parse-numbered_list_item
  "https://www.markdownguide.org/basic-syntax/#ordered-lists"
  [prefix]
  (fn [{:keys [id numbered_list_item has_children]}]
    (let [text (reduce str (map parse-text (:text numbered_list_item)))]
      (if has_children
        (str "1. " text "\n\n"
             (element->md (fetch-notion-children id)
                          prefix))
        (str "1. " text "\n")))))

(defn parse-todo
  "A TODO checkbox. If its checked, represent it as markdown"
  [prefix]
  (fn [{:keys [id has_children to_do]}]
    (let [text (reduce str (map parse-text (:text to_do)))
          check-and-text (str "- [" (if (:checked to_do) "x" " ") "] " text "\n")]
      (if has_children
        (str check-and-text
             (element->md (fetch-notion-children id)
                          prefix))
        (str check-and-text)))))

(defn parse-file
  "Returns a '[type](link)' "
  [kind]
  (fn [block]
    (let [element (kind block)
          url (or (-> element :file :url)
                  (-> element :external :url)
                  (-> element :url))
          caption (if (seq (:caption element))
                    (reduce str (map parse-text (:caption element)))
                    (name kind))]
      (str "[" caption "](" url ")\n"))))

(defn parse-divider
  [_]
  (str "\n---\n"))

(defn parse-equation [prefix]
  (fn [{:keys [equation]}]
    (code->md
      prefix
      (:expression equation)
      "undefined")))

(defn parse-toggle!
  "Returns a <details> html element that will be rendered as a button to fold content"
  [{:keys [toggle has_children id]}]
  (str
    "<details>\n"
    "<summary>\n"
    (reduce str (map parse-text (:text toggle)))
    "\n</summary>\n\n"
    (when has_children
      (element->md (fetch-notion-children id)))
    "</details>\n"))

(defn parse-child-page!
  "Creates a file and returns a link to it"
  [current-file]
  (fn [{:keys [child_page has_children id]}]
    (let [{:keys [title]} child_page]
      (if has_children
        (let [file-name (-> title
                            (str/replace "/" "-")
                            (str ".md"))
              link-to-file (str/replace file-name " " "%20")]
          (->md {:results (into [(make-heading title)]
                                (:results (@fetch-children id)))}
                (str docs-path file-name))
          (str "[" title "](" (if (= current-file "README.md")
                                docs-path
                                "./") link-to-file ")\n"))
        title))))

(defn parse-template!
  "Parses the template type from notion."
  [{:keys [template has_children id]}]
  (let [{:keys [text]} template
        prefix (reduce str (map parse-text text))]
    (if has_children
      (str prefix "\n"
           (element->md (fetch-notion-children id)))
      prefix)))

(defn parse-synced-block!
  "Parses a synced_block, returns it's original content"
  [{:keys [synced_block has_children id]}]
  (if has_children
    (element->md (fetch-notion-children id))
    (element->md (-> (get-in synced_block [:synced_from :block_id])
                     (@fetch-children)
                     :results))))

(defn parse-link-to-page!
  "Returns the content from the page. 
   Doesn't return a markdown link"
  [{:keys [link_to_page]}]
  (element->md (->> (:page_id link_to_page)
                    (@fetch-children)
                    :results)))

(defn parse-column-list!
  "Returns the children content"
  [prefix file-name]
  (fn
    [{:keys [id has_children]}]
    (when has_children
      (element->md (fetch-notion-children id)
                   prefix file-name))))

(defn parse-column!
  "Returns the children content"
  [prefix file-name]
  (fn
    [{:keys [id has_children]}]
    (when has_children
      (element->md (fetch-notion-children id)
                   prefix file-name))))

(defn parsers
  "input: type of notion element; optional prefix for recursiveness; current file-name
   output: (may create an md file) string with markdown content"
  [kind prefix file-name]
  (or
    (kind
      {:paragraph parse-paragraph
       :image parse-image!
       :code (parse-code prefix)
       :callout (parse-callout (str prefix "\t"))
       :toggle parse-toggle!
       :bulleted_list_item (parse-bulleted-list-item (str prefix "\t"))
       :link_to_page parse-link-to-page!
       :column_list (parse-column-list! prefix file-name)
       :column (parse-column! prefix file-name)
       :numbered_list_item (parse-numbered_list_item (str prefix "\t"))
       :synced_block parse-synced-block!
       :to_do (parse-todo (str prefix "\t"))
       :equation (parse-equation prefix)
       :divider parse-divider
       :template parse-template!
       :quote (parse-quote (str prefix ">"))
       :video (parse-file :video)
       :file (parse-file :file)
       :pdf (parse-file :pdf)
       :bookmark (parse-file :bookmark)
       :embed (parse-file :embed)
       :heading_1 (parse-heading :heading_1)
       :heading_2 (parse-heading :heading_2)
       :heading_3 (parse-heading :heading_3)
       :child_page (parse-child-page! file-name)})
    placeholder-parser))

(defn element->content
  "input: prefix (optional) to prepend to md content if nested
          file-name the current md file-name
          element: a block from the notion API
   output: (may create an md files) string with markdown content"
  [prefix file-name]
  (fn [element]
    (let [k (keyword (:type element))
          parser (parsers k prefix file-name)]
      (parser element))))

(defn element->md
  "input: :results (coll) from the notion API https://api.notion.com/v1/blocks/<id>/children
   opts: prefix: nil or a prefix
   file-name: the current md file being generated
   output: a markdown formatted string (and possible md files created)"
  [results & opts]
  (let [prefix (first opts)
        file-name (second opts)]
    (apply str
           (map (fn [item] (str prefix item "\n"))
                (map (element->content prefix file-name) results)))))

(defn ->md
  "input: :results (coll) from the notion API https://api.notion.com/v1/blocks/<id>/children
   file-name: the current md file being generated
   output: an md file with markdown formatted string as content (and other possible md files created) "
  [{:keys [results]} file-name]
  (println "creating " file-name)
  (spit file-name
        (element->md results nil file-name)))

(def secret (env :notion-api-secret))
(def page-id (env :notion-page-id))

(defn create-readme!
  [secret page-id]
  (io/make-parents docs-path "keep.md")
  (reset! fetch-children (http-client/fetch-children secret))
  (->md (@fetch-children page-id)
        "README.md")
  (println "Done"))

(def help "You must set the NOTION_API_SECRET and NOTION_PAGE_ID environment variables.\nOther way to use this program is to pass 2 parameters: secret and page_id.\nExample:\n./notion-to-md secret_j892013joiaiuae0912401720293uojdioaw8903428 b861e277e4b14f44bc34f4b562d7e4ed ")

(defn -main
  [& args]
  (cond
    (and secret page-id) (create-readme! secret page-id)
    (= 2 (count args)) (let [[secret page-id] args] (create-readme! secret page-id))
    :else (println help)))
