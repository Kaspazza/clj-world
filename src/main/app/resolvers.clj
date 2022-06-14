(ns app.resolvers
  (:require
    [com.wsscode.pathom.core :as p]
    [clojure.core.async :as a]
    [app.datomic :as datomic]
    [com.wsscode.pathom.connect :as pc]))

(def content-table
  {1 {:content/id 1 :content/type :project :content/title "Rock paper scissor" :content/desc "First content text"}
   2 {:content/id 2 :content/type :project :content/title "Guess number" :content/desc "Some more shiet"}
   3 {:content/id 3 :content/type :project :content/title "Hangman" :content/desc "Hello hello give me a blow"}
   4 {:content/id 4 :content/type :project :content/title "Testowy" :content/desc "I like bussy"}
   5 {:content/id 5 :content/type :theory :content/title "I am a title" :content/desc "Karolinka Karolinka du0pnka"}
   6 {:content/id 6 :content/type :theory :content/title "Star wars" :content/desc "czesc mruweczko"}
   7 {:content/id 7 :content/type :exercise :content/title "Hello world" :content/desc "You need to solve this task come on"}})

;; Given :content/id, this can generate the details of a content
(pc/defresolver content-resolver [env {:content/keys [id]}]
  {::pc/input  #{:content/id}
   ::pc/output [:content/type :content/title :content/desc]}
  (get content-table id))

(pc/defresolver category-resolver [_ {id :category/id}]
  {::pc/input #{:category/id}
   ::pc/output [{:category/content [:content/id]}]}
  (let [ids (datomic/content-by-type id)
        ids2 (into [] (map (fn [content] (first content)) ids))
        test (prn "ids: " ids2)]
  {:category/content ids2}
  #_(case id
    :project {:category/content [{:content/id 1} {:content/id 2} {:content/id 3} {:content/id 4}]}
    :theory {:category/content [{:content/id 5} {:content/id 6}]}
    :exercise {:category/content [{:content/id 7}]})))

(def resolvers [content-resolver category-resolver])