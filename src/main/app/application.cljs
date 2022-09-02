(ns app.application
  (:require
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.networking.http-remote :as http]
    [com.fulcrologic.fulcro.components :as comp]))

(defonce APP (app/fulcro-app
               {:remotes {:remote (http/fulcro-http-remote {})}
                :render-middleware (when goog.DEBUG js/holyjak.fulcro_troubleshooting.troubleshooting_render_middleware)}))

