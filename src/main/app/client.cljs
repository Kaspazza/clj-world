(ns app.client
  (:require
    [app.application :refer [APP]]
    [app.mutations]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [app.categories :refer [Categories CategoryHeader]]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.rad.routing.html5-history :as hist5 :refer [html5-history]]
    [com.fulcrologic.rad.routing :as routing]
    [com.fulcrologic.rad.routing.history :as history]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr :refer [defrouter]]
    [com.fulcrologic.fulcro.data-fetch :as df]))


(defrouter RootRouter [this {:keys [current-state] :as props}]
  {:router-targets [Categories]}
  (case current-state
    :pending (dom/div "Loading...")
    :failed (dom/div "Failed!")
    (dom/div "No route selected.")))

(def ui-root-router (comp/factory RootRouter))

(defsc Root [this {:root/keys [router categories]}]
  {:query [{:root/router (comp/get-query RootRouter)}
           {:root/categories (comp/get-query Categories)}]
   :initial-state (fn [p] {:root/router {}
                           :root/categories (comp/get-initial-state Categories)})}
  (dom/div
    (ui-root-router router)))


(defn ^:export init
  "Shadow-cljs sets this up to be our entry-point function. See shadow-cljs.edn `:init-fn` in the modules of the main build."
  []
  (app/set-root! APP Root {:initialize-state? true})
  (dr/initialize! APP)
  (df/load! APP [:category/id :project] CategoryHeader {:focus [:category/content]})
  (history/install-route-history! APP (html5-history))
  (hist5/restore-route! APP Categories {})
  (routing/route-to! APP Categories {:category/id "project"})
  (app/mount! APP Root "app" {:initialize-state? false})
  (js/console.log "Loaded"))

(defn ^:export refresh
  "During development, shadow-cljs will call this on every hot reload of source. See shadow-cljs.edn"
  []
  ;; re-mounting will cause forced UI refresh, update internals, etc.
  (app/mount! APP Root "app")
  ;; As of Fulcro 3.3.0, this addition will help with stale queries when using dynamic routing:
  (comp/refresh-dynamic-queries! APP)
  (js/console.log "Hot reload"))
