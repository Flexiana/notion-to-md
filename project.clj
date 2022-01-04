(defproject com.flexiana/notion-to-md "0.1.2"
  :description "Notion To Markdown converter"
  :uberjar-name "notion-to-md.jar"
  :test-paths ["test"]
  :source-paths ["src"]
  :main notion-to-md.core
  :deploy-repositories [["clojars" {:sign-releases false}]]
  :dependencies [[org.clojure/data.json "0.2.6"]
                 [environ "1.0.0"]
                 [clj-http "3.12.3"]
                 [org.clojure/clojure "1.10.0"]]
  :profiles {:local
             {:dependencies [[clj-http "3.12.3"]
                             [com.flexiana/notion-to-md "0.1.2"]]}}
  :aliases {"notion-to-md"  ["with-profile" "local" "run" "-m" "notion-to-md.core"]}
  :repl-options {:init-ns notion-to-md.core})
