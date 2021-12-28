(ns notion-to-md.http-client
  (:require
    [clj-http.client :as client]
    [clojure.data.json :as json]))

(defn body->clj [response]
  (->> response
       :body
       json/read-json))

(defn fetch-image [url]
  (:body (client/get url {:as :stream
                          :async? false})))

(defn fetch-children [secret]
  (fn [id]
    (body->clj
      (client/get
        (str "https://api.notion.com/v1/blocks/" id "/children")
        {:headers {"Authorization" (str "Bearer " secret)
                   "Notion-Version" "2021-08-16"}}))))
