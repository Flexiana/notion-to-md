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

(defn- fetch-children-paginated [secret id & opts]
  (body->clj
    (client/get
      (str "https://api.notion.com/v1/blocks/" id "/children"
           (when-let [start_cursor (first opts)]
             (str "/?start_cursor=" start_cursor)))
      {:headers {"Authorization" (str "Bearer " secret)
                 "Notion-Version" "2021-08-16"}})))

(defn fetch-children
  "Returns a coll of blocks"
  [secret]
  (fn [id]
    (loop [response (fetch-children-paginated secret id)
           result []]
      (if-not (:has_more response)
        (conj result response)
        (recur (fetch-children-paginated secret id (:next_cursor response))
               (conj result response))))))
