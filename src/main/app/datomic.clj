(ns app.datomic
  (:require [datomic.client.api :as d]
            [datomic.dev-local :as dl]))

;; Explicitly requesting local client TODO change to datomic cloud
(def config {:server-type :ion
             :system "clj-world"
             :region "eu-west-2"
             :endpoint "https://xrjzcnnv55.execute-api.eu-west-2.amazonaws.com"
             })

(defn client [] (d/client config))

(defn conn [] (d/connect (client) {:db-name "clj-world"}))

(def db (d/db (conn)))

(defn run-dev []
  (dl/divert-system {:system "clj-world"}))

(def content-schema
  [{:db/ident :content/id
    :db/valueType :db.type/uuid
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one
    :db/doc "The id of content"}

   {:db/ident :content/type
    :db/valueType :db.type/keyword
    :db/cardinality :db.cardinality/one
    :db/doc "The category (type) of content"}

   {:db/ident :content/title
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The title of lesson"}

   {:db/ident :content/desc
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The description of lesson"}

   {:db/ident :content/img
    :db/valueType :db.type/uri
    :db/cardinality :db.cardinality/one
    :db/doc "Preview image of lesson"}])


(defn content-by-type [type]
  (d/q '[:find (pull ?e [:content/title :content/desc :content/type :content/id :content/img])
         :in $ ?type
         :where [?e :content/type ?type]] (d/db (conn)) type))

(defn content-by-id [id]
  (d/q '[:find (pull ?e [:content/title :content/desc :content/type :content/id :content/img])
         :in $ ?id
         :where [?e :content/id ?id]] (d/db (conn)) id))

(comment
  (import (java.util UUID))

  (defn uuid [] (UUID/randomUUID))

  (def fake-data
    [{:content/id (uuid)
      :content/type :project
      :content/title "Rock paper & scissor"
      :content/desc "First content text"}

     {:content/id (uuid)
      :content/type :project
      :content/title "Guess number"
      :content/desc "In this project we will learn how to"}

     {:content/id (uuid)
      :content/type :project
      :content/title "Hangman"
      :content/desc "Let's hang out together"}

     {:content/id (uuid)
      :content/type :theory
      :content/title "JVM"
      :content/desc "Today we will learn about JVM"}

     {:content/id (uuid)
      :content/type :theory
      :content/title "Data structures"
      :content/desc "Did you ever wonder how the vector works internally?"}

     {:content/id (uuid)
      :content/type :exercise
      :content/title "FizzBuzz"
      :content/desc "The most known classic of coding exercises"}])

  ;; create db
  (d/create-database client {:db-name "clj-world"})

  ;; add new schema
  (d/transact conn {:tx-data content-schema})

  ;; add some data
  (d/transact conn {:tx-data fake-data})

  (d/transact conn {:tx-data [{:content/id (uuid)
                               :content/title "Rock paper & scissor"}]})

  ;; connection
  (d/db conn)

  (content-by-type :project)

  )