(ns app.client
  (:require
    [app.application :refer [APP]]
    [app.mutations]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [app.ui.categories :refer [Categories]]
    [app.ui.lesson :as lesson :refer [Lesson]]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.rad.routing.history :as history]
    [com.fulcrologic.rad.routing.html5-history :as hist5 :refer [html5-history]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr :refer [defrouter]]
    [com.fulcrologic.rad.routing :as routing]))

(defrouter RootRouter [this {:keys [current-state]}]
  {:router-targets [Categories Lesson]}
  (case current-state
    :pending (dom/div "Loading...")
    :failed (dom/div "Failed!")
    (dom/div "No route selected.")))

(def ui-root-router (comp/factory RootRouter))

(defsc Root [_this {:root/keys [router
                                ;categories lesson
                                ]}]
  {:query [{:root/router (comp/get-query RootRouter)}
           {:root/categories (comp/get-query Categories)}
           ;{:root/lesson (comp/get-query Lesson)}
           ]
   :initial-state (fn [_] {:root/router {}
                           :root/categories (comp/get-initial-state Categories)
                           })}
  (dom/div
    (ui-root-router router)))

(defn ^:export init
  "Shadow-cljs sets this up to be our entry-point function. See shadow-cljs.edn `:init-fn` in the modules of the main build."
  []
  (app/set-root! APP Root {:initialize-state? true})
  (dr/initialize! APP)
  (history/install-route-history! APP (html5-history))
  (hist5/restore-route! APP Categories {:category-id "project"})
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

(comment
  (-> APP (::app/state-atom) deref)

  )