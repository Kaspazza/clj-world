(ns app.resolvers
  (:require
    [app.datomic :as datomic]
    [com.wsscode.pathom.connect :as pc]))

#_(pc/defresolver content-resolver [env {:content/keys [id]}]
  {::pc/input  #{:content/id}
   ::pc/output [:content/type :content/title :content/desc]})

(pc/defresolver category-resolver [_ {id :category/id}]
  {::pc/input #{:category/id}
   ::pc/output [{:category/content [:content/id]}]}
  (let [content-data (datomic/content-by-type id)
        formatted-content (into [] (map (fn [content] (first content)) content-data))]
  {:category/content formatted-content}))

(def resolvers [category-resolver])