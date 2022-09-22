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

   {:db/ident :lesson/full-desc
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Detailed description of lesson"}

   {:db/ident :lesson/img
    :db/valueType :db.type/uri
    :db/cardinality :db.cardinality/one
    :db/doc "Preview image of lesson"}

   {:db/ident :lesson/editors
    :db/valueType :db.type/tuple
    :db/cardinality :db.cardinality/one
    :db/doc "Tuple containing editor states"}])




(defn lesson-by-type [type]
  (d/q '[:find (pull ?e [:lesson/id])
         :in $ ?type
         :where [?e :lesson/type ?type]] db type))

(defn lesson-by-id [id]
  (d/q '[:find (pull ?e [:lesson/title :lesson/desc :lesson/full-desc :lesson/type :lesson/id :lesson/img])
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
      :lesson/desc "The most known classic of coding exercises"
      :lesson/full-desc "Jedzą, piją, lulki palą,\nTańce, hulanka, swawola;\nLedwie karczmy nie rozwalą,\nHa, ha! Hi, hi! hejże! hola!\nTwardowski siadł w końcu stoła,\nPodparł się w boki jak basza:\n„Hulaj dusza! hulaj!” woła,\nŚmieszy, tumani, przestrasza. \nŻołnierzowi, co grał zucha,\nWszystkich łaje i potrąca,\nŚwisnął szablą koło ucha:\nJuż z żołnierza masz zająca.\nNa patrona z trybunału,\nCo milczkiem wypróżniał rondel,\nZadzwonił kieską, pomału:\nZ patrona robi się kondel.\nSzewcu w nos wyciął trzy szczutki,\nDo łba przymknął trzy rureczki,\nCmoknął: cmok! i gdańskiej wódki\nWytoczył ze łba pół beczki. \nWtem, gdy wódkę pił z kielicha,\nKielich zaświstał, zazgrzytał;\nPatrzy na dno: — „Co u licha?\nPo coś tu, kumie, zawitał?”\nDiablik to był w wódce na dnie:\nIstny Niemiec, sztuczka kusa;\nSkłonił się gościom układnie,\nZdjął kapelusz i dał susa.\nZ kielicha aż na podłogę\nPada, rośnie na dwa łokcie,\nNos jak haczyk, kurzą nogę,\nI krogulcze ma paznokcie. \n„A, Twardowski… witam bracie!”\nTo mówiąc, bieży obcesem:\n„Cóż to, czyliż mię nie znacie?\nJestem Mefistofelesem.\nWszak ze mnąś na Łysej Górze\nRobił o duszę zapisy:\nCyrograf na byczej skórze\nPodpisałeś ty i bisy.\nMiały słuchać twego rymu;\nTy, jak dwa lata przebiegą,\nMiałeś pojechać do Rzymu,\nBy cię tam porwać jak swego.\nJuż i siedem lat uciekło,\nCyrograf nadal nie służy:\nTy, czarami dręcząc piekło,\nAni myślisz o podróży.\nAle zemsta, choć leniwa,\nNagnała cię w nasze sieci:\nTa karczma Rzym się nazywa…\nKładę areszt na waszeci”. \nTwardowski ku drzwiom się kwapił \nNa takie dictum acerbum;\nDiabeł za kontusz ułapił:\n„A gdzie jest nobile verbum?” \nCo tu począć? kusa rada,\nPrzyjdzie już nałożyć głową…\nTwardowski na koncept wpada\nI zadaje trudność nową.\n"}])

  ;; create db
  (d/create-database (client) {:db-name "clj-world-dev"})

  ;; add new schema
  (d/transact conn {:tx-data lesson-schema})

  ;; add some data
  (d/transact conn {:tx-data fake-data})

  (d/transact conn {:tx-data [{:lesson/id (uuid)
                              :lesson/type :exercise
                              :lesson/title "FizzBuzz"
                              :lesson/desc "The most known classic of coding exercises"
                              :lesson/full-desc "Jedzą, piją, lulki palą,\nTańce, hulanka, swawola;\nLedwie karczmy nie rozwalą,\nHa, ha! Hi, hi! hejże! hola!\nTwardowski siadł w końcu stoła,\nPodparł się w boki jak basza:\n„Hulaj dusza! hulaj!” woła,\nŚmieszy, tumani, przestrasza. \nŻołnierzowi, co grał zucha,\nWszystkich łaje i potrąca,\nŚwisnął szablą koło ucha:\nJuż z żołnierza masz zająca.\nNa patrona z trybunału,\nCo milczkiem wypróżniał rondel,\nZadzwonił kieską, pomału:\nZ patrona robi się kondel.\nSzewcu w nos wyciął trzy szczutki,\nDo łba przymknął trzy rureczki,\nCmoknął: cmok! i gdańskiej wódki\nWytoczył ze łba pół beczki. \nWtem, gdy wódkę pił z kielicha,\nKielich zaświstał, zazgrzytał;\nPatrzy na dno: — „Co u licha?\nPo coś tu, kumie, zawitał?”\nDiablik to był w wódce na dnie:\nIstny Niemiec, sztuczka kusa;\nSkłonił się gościom układnie,\nZdjął kapelusz i dał susa.\nZ kielicha aż na podłogę\nPada, rośnie na dwa łokcie,\nNos jak haczyk, kurzą nogę,\nI krogulcze ma paznokcie. \n„A, Twardowski… witam bracie!”\nTo mówiąc, bieży obcesem:\n„Cóż to, czyliż mię nie znacie?\nJestem Mefistofelesem.\nWszak ze mnąś na Łysej Górze\nRobił o duszę zapisy:\nCyrograf na byczej skórze\nPodpisałeś ty i bisy.\nMiały słuchać twego rymu;\nTy, jak dwa lata przebiegą,\nMiałeś pojechać do Rzymu,\nBy cię tam porwać jak swego.\nJuż i siedem lat uciekło,\nCyrograf nadal nie służy:\nTy, czarami dręcząc piekło,\nAni myślisz o podróży.\nAle zemsta, choć leniwa,\nNagnała cię w nasze sieci:\nTa karczma Rzym się nazywa…\nKładę areszt na waszeci”. \nTwardowski ku drzwiom się kwapił \nNa takie dictum acerbum;\nDiabeł za kontusz ułapił:\n„A gdzie jest nobile verbum?” \nCo tu począć? kusa rada,\nPrzyjdzie już nałożyć głową…\nTwardowski na koncept wpada\nI zadaje trudność nową.\n"}]})

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

  (lesson-by-type :exercise)

  (lesson-by-id #uuid "88ea5250-fafd-44c2-8d10-5aa69c942a41")

  )