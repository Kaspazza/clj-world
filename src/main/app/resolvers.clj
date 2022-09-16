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


(pc/defresolver lesson-resolver [env {:lesson/keys [id]}]
  {::pc/input  #{:lesson/id}
   ::pc/output [:lesson/type :lesson/title :lesson/desc {:lesson/editor [:editor/id]}]}
  (let [lesson-data (datomic/lesson-by-id id)
        formatted-lesson (ffirst lesson-data)
        with-editor (merge formatted-lesson {:lesson/editor {:editor/id 1}})]
    with-editor))

(pc/defresolver category-resolver [_ {id :category/id}]
  {::pc/input #{:category/id}
   ::pc/output [{:category/lessons [:lesson/id]}]}
  (let [lesson-data (datomic/lesson-by-type id)
        formatted-lesson (into [] (map (fn [lesson] (first lesson)) lesson-data))]
  {:category/lessons formatted-lesson}))

(def resolvers [lesson-resolver category-resolver editor-resolver])