(ns app.application
  (:require
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.networking.http-remote :as http]
    [com.fulcrologic.fulcro.components :as comp]))

(defonce APP (app/fulcro-app
               {:remotes {:remote (http/fulcro-http-remote {})}}))

