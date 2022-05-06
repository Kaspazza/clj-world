(ns app.resolvers
  (:require
    [com.wsscode.pathom.core :as p]
    [com.wsscode.pathom.connect :as pc]))

(def content-table
  {1 {:content/id 1 :content/type :project :content/text "First content text"}
   2 {:content/id 2 :content/type :project :content/text "Some more shiet"}
   3 {:content/id 3 :content/type :project :content/text "Hello hello give me a blow"}
   4 {:content/id 4 :content/type :project :content/text "I like bussy"}
   5 {:content/id 5 :content/type :theory :content/text "Karolinka Karolinka du0pnka"}
   6 {:content/id 6 :content/type :theory :content/text "czesc mruweczko"}
   7 {:content/id 7 :content/type :exercise :content/text "You need to solve this task come on"}})

;; Given :content/id, this can generate the details of a content
(pc/defresolver content-resolver [env {:content/keys [id]}]
  {::pc/input  #{:content/id}
   ::pc/output [:content/type :content/text]}
  (get content-table id))

(pc/defresolver category-resolver [_ {id :category/id}]
  {::pc/input #{:category/id}
   ::pc/output [{:category/content [:content/id]}]}
  (case id
    :projects {:category/content [{:content/id 1} {:content/id 2} {:content/id 3} {:content/id 4}]}
    :theory {:category/content [{:content/id 5} {:content/id 6}]}
    :exercises {:category/content [{:content/id 7}]}))

(def resolvers [content-resolver category-resolver])