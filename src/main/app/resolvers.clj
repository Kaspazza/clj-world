(ns app.resolvers
  (:require
    [app.datomic :as datomic]
    [com.wsscode.pathom.connect :as pc]))

(pc/defresolver editor-resolver [env {:editor/keys [id]}]
  {::pc/input  #{:editor/id}
   ::pc/output [:editor/text]}
  (let [datomic-text datomic/sample
        content {:editor/id id
                 :editor/text datomic-text}]
    content))


(pc/defresolver content-resolver [env {:content/keys [id]}]
  {::pc/input  #{:content/id}
   ::pc/output [:content/type :content/title :content/desc {:content/editor [:editor/id]}]}
  (let [content-data (datomic/content-by-id id)
        formatted-content (ffirst content-data)
        with-editor (merge formatted-content {:content/editor {:editor/id 1}})]
    with-editor))

(pc/defresolver category-resolver [_ {id :category/id}]
  {::pc/input #{:category/id}
   ::pc/output [{:category/content [:content/id]}]}
  (let [content-data (datomic/content-by-type id)
        formatted-content (into [] (map (fn [content] (first content)) content-data))]
  {:category/content formatted-content}))

(def resolvers [content-resolver category-resolver editor-resolver])