(ns app.datomic
  (:require [datomic.client.api :as d]
            [datomic.dev-local :as dl]))

;; Explicitly requesting local client TODO change to datomic cloud
(def config {:server-type :ion
             :system "clj-world"
             :region "eu-west-2"
             :endpoint "https://xrjzcnnv55.execute-api.eu-west-2.amazonaws.com"
             })

(def dev-config {:server-type :dev-local
                 :system "dev"})

(def client (d/client dev-config))

(def conn (d/connect client {:db-name "clj-world-dev"}))

(def db (d/db conn))

(defn run-dev []
  (dl/divert-system {:system "clj-world"}))

(def lesson-schema
  [{:db/ident :lesson/id
    :db/valueType :db.type/uuid
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one
    :db/doc "The id of lesson"}

   {:db/ident :lesson/type
    :db/valueType :db.type/keyword
    :db/cardinality :db.cardinality/one
    :db/doc "The category (type) of lesson"}

   {:db/ident :lesson/title
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The title of lesson"}

   {:db/ident :lesson/desc
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The description of lesson"}

   {:db/ident :lesson/img
    :db/valueType :db.type/uri
    :db/cardinality :db.cardinality/one
    :db/doc "Preview image of lesson"}])


(defn lesson-by-type [type]
  (d/q '[:find (pull ?e [:lesson/id])
         :in $ ?type
         :where [?e :lesson/type ?type]] db type))

(defn lesson-by-id [id]
  (d/q '[:find (pull ?e [:lesson/title :lesson/desc :lesson/type :lesson/id :lesson/img])
         :in $ ?id
         :where [?e :lesson/id ?id]] db id))

(def sample
  "
  (defn fizz-buzz [n]\n  (condp (fn [a b] (zero? (mod b a))) n\n    15 \"fizzbuzz\"\n    3  \"fizz\"\n    5  \"buzz\"\n    n))

  (comment
  (fizz-buzz 1)
  (fizz-buzz 3)
  (fizz-buzz 5)
  (fizz-buzz 15)
  (fizz-buzz 17)
  (fizz-buzz 42))")


(comment
  (import (java.util UUID))

  (defn uuid [] (UUID/randomUUID))

  (def fake-data
    [{:lesson/id (uuid)
      :lesson/type :project
      :lesson/title "Rock paper & scissor"
      :lesson/desc "First lesson text"}

     {:lesson/id (uuid)
      :lesson/type :project
      :lesson/title "Guess number"
      :lesson/desc "In this project we will learn how to"}

     {:lesson/id (uuid)
      :lesson/type :project
      :lesson/title "Hangman"
      :lesson/desc "Let's hang out together"}

     {:lesson/id (uuid)
      :lesson/type :theory
      :lesson/title "JVM"
      :lesson/desc "Today we will learn about JVM"}

     {:lesson/id (uuid)
      :lesson/type :theory
      :lesson/title "Data structures"
      :lesson/desc "Did you ever wonder how the vector works internally?"}

     {:lesson/id (uuid)
      :lesson/type :exercise
      :lesson/title "FizzBuzz"
      :lesson/desc "The most known classic of coding exercises"}])

  ;; create db
  (d/create-database (client) {:db-name "clj-world-dev"})

  ;; add new schema
  (d/transact conn {:tx-data lesson-schema})

  ;; add some data
  (d/transact conn {:tx-data fake-data})

  (d/transact conn {:tx-data [{:lesson/id #uuid"1b2d87cb-ddfa-4b6c-b991-4c48301e41a0"
                               :lesson/editor "
  (defn fizz-buzz [n]\n  (condp (fn [a b] (zero? (mod b a))) n\n    15 \"fizzbuzz\"\n    3  \"fizz\"\n    5  \"buzz\"\n    n))

  (comment
  (fizz-buzz 1)
  (fizz-buzz 3)
  (fizz-buzz 5)
  (fizz-buzz 15)
  (fizz-buzz 17)
  (fizz-buzz 42))"}]})

  ;; connection
  (d/db conn)

  (lesson-by-type :project)

  )