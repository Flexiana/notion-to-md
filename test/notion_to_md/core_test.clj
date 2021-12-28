(ns notion-to-md.core-test
  (:require
    [notion-to-md.core :as c]
    [clojure.test :as t]))

(def file-parser
  (c/parsers :file nil "file-name.md"))

(def to_do-parser
  (c/parsers :to_do nil "file-name.md"))

(def heading3-parser
  (c/parsers :heading_3 nil "file-name.md"))

(def quote-parser
  (c/parsers :quote nil "file-name.md"))

(def callout-parser
  (c/parsers :callout nil "file-name.md"))

(def pdf-parser
  (c/parsers :pdf nil "file-name.md"))

(def bookmark-parser
  (c/parsers :bookmark nil "file-name.md"))

(defn file-fixture [kind]
  {:archived false
   :type (name kind)
   kind
     {:caption []
      :type (name kind)
      :file
        {:url "https://url-file.pdf"
         :expiry_time "2021-12-21T15:05:22.492Z"}}})

(defn file-fixture-external [kind]
  {:type (name kind)
   kind
     {:caption []
      :type "external"
      :external
        {:url "https://url-external-file.pdf"}}})

(def todo-fixture
  {:object "block"
   :id "a640e37c-71b9-4680-8ef7-54ab3cea7f97"
   :created_time "2021-12-15T17:57:00.000Z"
   :last_edited_time "2021-12-15T17:57:00.000Z"
   :has_children false
   :archived false
   :type "to_do"
   :to_do
     {:text
        [{:type "text"
          :text {:content "Click " :link nil}
          :annotations
            {:bold false
             :italic false
             :strikethrough false
             :underline false
             :code false
             :color "default"}
          :plain_text "Click "
          :href nil}
         {:type "text"
          :text {:content "Templates" :link nil}
          :annotations
            {:bold false
             :italic false
             :strikethrough false
             :underline false
             :code true
             :color "default"}
          :plain_text "Templates"
          :href nil}
         {:type "text"
          :text
            {:content " in your sidebar to get started with pre-built pages"
             :link nil}
          :annotations
            {:bold false
             :italic false
             :strikethrough false
             :underline false
             :code false
             :color "default"}
          :plain_text
            " in your sidebar to get started with pre-built pages"
          :href nil}]
      :checked true}})

(def text-fixture
  {:annotations
     {:bold false
      :italic false
      :strikethrough false
      :underline false ; not maintained
      :code false
      :color "default"} ; not maintained
   :text {:content "👋 Welcome to Notion!"}
   :plain_text "👋 Welcome to Notion!"})

(def quote-fixture
  {:object "block"
   :type "quote"
   :quote {:text
             [{:type "text"
               :text {:content "Lacinato kale"}}]}})

(def callout-fixture
  {:object "block"
   :id "12367505-aacb-4cf3-9714-30ff8728bbef"
   :created_time "2021-12-20T15:21:00.000Z"
   :last_edited_time "2021-12-20T15:22:00.000Z"
   :has_children false
   :archived false
   :type "callout"
   :callout
     {:text
        [{:type "text"
          :text {:content "This is a callout!!!" :link nil}
          :annotations
            {:bold false
             :italic false
             :strikethrough false
             :underline false
             :code false
             :color "default"}
          :plain_text "This is a callout!!!"
          :href nil}]
      :icon {:type "emoji" :emoji "🛠"}}})
(def heading-fixture
  {:type "heading_3"
   :heading_3
     {:text
        [{:type "text"
          :text {:content "  Heading Three! " :link nil}
          :annotations
            {:bold true
             :italic false
             :strikethrough true
             :underline false
             :code true
             :color "default"}
          :plain_text "  Heading Three! "
          :href nil}
         {:type "text"
          :text {:content "Italic" :link nil}
          :annotations
            {:bold false
             :italic true
             :strikethrough false
             :underline false
             :code false
             :color "default"}
          :plain_text "Italic"
          :href nil}
         {:type "text"
          :text {:content " " :link nil}
          :annotations
            {:bold false
             :italic false
             :strikethrough false
             :underline false
             :code false
             :color "default"}
          :plain_text " "
          :href nil}
         {:type "text"
          :text {:content "code" :link nil}
          :annotations
            {:bold false
             :italic false
             :strikethrough false
             :underline false
             :code true
             :color "default"}
          :plain_text "code"
          :href nil}]}})

(defn ->enable [text-fixture k]
  (assoc-in text-fixture [:annotations k] true))

(t/deftest rich-text
  (t/testing "Rich text"
    (t/is (= "  **~~`Heading Three!`~~** "
             (c/apply-annotations (-> heading-fixture
                                      :heading_3
                                      :text
                                      first))))
    (t/is (= "👋 Welcome to Notion!" (c/apply-annotations text-fixture)))
    (t/is (= "**👋 Welcome to Notion!**" (c/apply-annotations (->enable text-fixture :bold))))
    (t/is (= "**`👋 Welcome to Notion!`**" (c/apply-annotations
                                               (-> (->enable text-fixture :code)
                                                   (->enable :bold)))))))

(t/deftest rich-text-not-maintained-types
  (t/testing "md doesn't support underline nor color"
    (t/is (= "👋 Welcome to Notion!" (c/apply-annotations (->enable text-fixture :underline))))
    (t/is (= "👋 Welcome to Notion!" (c/apply-annotations
                                         (assoc-in text-fixture [:annotations :colr] "red"))))))

(t/deftest parse-heading
  (t/testing "headings has a # and linebreaks"
    (t/is (= "\n###   **~~`Heading Three!`~~** *Italic* `code`\n" (heading3-parser heading-fixture)))))

(t/deftest parse-callout
  (t/testing "callout"
    (t/is (= "- 🛠  This is a callout!!!\n" (callout-parser callout-fixture)))))

(t/deftest parse-quote
  (t/testing "quotes"
    (t/is (= "> Lacinato kale\n" (quote-parser quote-fixture)))))

(t/deftest parse-todo
  (t/testing "shows [x] or [ ]"
    (t/is (= "- [x] Click `Templates` in your sidebar to get started with pre-built pages\n" (to_do-parser todo-fixture)))))

(t/deftest parse-file
  (t/testing "shows a link"
    (t/is (= "[file](https://url-external-file.pdf)\n" (file-parser (file-fixture-external :file))))
    (t/is (= "[file](https://url-file.pdf)\n" (file-parser (file-fixture :file))))))

(t/deftest parse-pdf
  (t/testing "shows a link"
    (t/is (= "[pdf](https://url-external-file.pdf)\n" (pdf-parser (file-fixture-external :pdf))))
    (t/is (= "[pdf](https://url-file.pdf)\n" (pdf-parser (file-fixture :pdf))))))

(def bookmark-fixture
  {:object "block"
   :id "b6288821-ccab-4351-bd44-0198af97ecc3"
   :created_time "2021-12-21T14:27:00.000Z"
   :last_edited_time "2021-12-21T14:27:00.000Z"
   :has_children false
   :archived false
   :type "bookmark"
   :bookmark {:caption [] :url "http://www.google.com"}})

(t/deftest parse-bookmark
  (t/testing "shows a link"
    (t/is (= "[bookmark](http://www.google.com)\n" (bookmark-parser bookmark-fixture)))
    (t/is (= "[Regular text**bold**](http://www.google.com)\n"
             (bookmark-parser
               (assoc-in bookmark-fixture [:bookmark :caption]
                         [{:type "text"
                           :text {:content "Regular text" :link nil}
                           :annotations
                             {:bold false
                              :italic false
                              :strikethrough false
                              :underline false
                              :code false
                              :color "default"}
                           :plain_text "Regular text"
                           :href nil}
                          {:type "text"
                           :text {:content "bold" :link nil}
                           :annotations
                             {:bold true
                              :italic false
                              :strikethrough false
                              :underline false
                              :code false
                              :color "default"}
                           :plain_text "bold"
                           :href nil}]))))))
