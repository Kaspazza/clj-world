(ns app.client
  (:require
    [app.application :refer [APP]]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [app.mutations :as m]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.rad.routing.html5-history :as hist5 :refer [html5-history]]
    [com.fulcrologic.rad.routing :as routing]
    [com.fulcrologic.rad.routing.history :as history]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr :refer [defrouter]]
    [com.fulcrologic.fulcro.data-fetch :as df]))

(defsc Content [this {:content/keys [id type title desc] :as props}]
  {:query [:content/id :content/type :content/title :content/desc]
   :ident (fn [] [:content/id (:content/id props)])}
  (dom/li {:className "relative"}
    (dom/div {:className "group block w-full aspect-w-10 aspect-h-7 rounded-lg bg-gray-100 focus-within:ring-2 focus-within:ring-offset-2 focus-within:ring-offset-gray-100 focus-within:ring-indigo-500 overflow-hidden"}
      (dom/img {:src "https://techcrunch.com/wp-content/uploads/2015/04/codecode.jpg?w=1390&crop=1"
                :alt "Lesson picture"
                :className "object-cover pointer-events-none group-hover:opacity-75"})
      (dom/button {:type "button"
                   :className "absolute inset-0 focus:outline-none"
                   :onClick (fn [_e] )}
        (dom/span {:className "sr-only"} "View details for " title)))
    (dom/p {:className "mt-2 block text-sm font-medium text-gray-900 truncate pointer-events-none"} title)
    (dom/p {:className "block text-sm font-medium text-gray-500 pointer-events-none"} desc)))

(def ui-content (comp/factory Content {:keyfn :content/id}))


(declare Categories)

(defsc CategoryHeader
  [this {:category/keys [id content] category-name :category/name :ui/keys [first? last? active?]}]
  {:query [:category/id :category/name {:category/content (comp/get-query Content)} :ui/first? :ui/last? :ui/active?]
   :ident :category/id}
  (dom/button {:key category-name
               :onClick (fn [_e]
                          (df/load! this [:category/id id] CategoryHeader {:focus [:category/content]})
                          (comp/transact! this `[(m/set-active-category {:chosen-id ~id})])
                          (routing/route-to! APP Categories {:category/id (name id)}))
               :classes [(if active? "text-gray-900" "text-gray-500 hover:text-gray-700")
                         (when first? "rounded-l-lg")
                         (when last? "rounded-r-lg")
                         "group relative min-w-0 flex-1 overflow-hidden bg-white py-4 px-4 text-sm font-medium text-center hover:bg-gray-50 focus:z-10"]
               :aria-label (if active? "page" nil)}
    (dom/span category-name)
    (dom/span {:aria-hidden "true" :classes [(if active? "bg-indigo-500" "bg-transparent") "absolute inset-x-0 bottom-0 h-0.5"]})))

(def ui-category-header (comp/factory CategoryHeader))

(defsc Categories [this {:categories/keys [project theory exercise] :as props}]
  {:query [{:categories/project (comp/get-query CategoryHeader)}
           {:categories/theory (comp/get-query CategoryHeader)}
           {:categories/exercise (comp/get-query CategoryHeader)}]
   :route-segment ["categories" :category/id]
   :initial-state (fn [_] {:categories/project {:category/id :project :category/name "Projects" :category/content [] :ui/first? true :ui/last? false :ui/active? true}
                           :categories/theory {:category/id :theory :category/name "Theory" :category/content [] :ui/first? false :ui/last? false :ui/active? false}
                           :categories/exercise {:category/id :exercise :category/name "Exercises" :category/content [] :ui/first? false :ui/last? true :ui/active? false}})
   :ident (fn [] [:component/id ::Categories])}
  (dom/div
    (dom/nav {:classes ["relative z-0 rounded-lg shadow flex divide-x divide-gray-200"] :aria-label "Tabs"}
      (ui-category-header project)
      (ui-category-header theory)
      (ui-category-header exercise))
    (dom/ul {:role "list" :className "grid grid-cols-2 gap-x-4 gap-y-8 sm:grid-cols-3 sm:gap-x-6 lg:grid-cols-4 xl:gap-x-8 mt-4 mx-4"}
      (map #(ui-content %) (:category/content (first (filter (fn [item]
                                                               (:ui/active? item)) [project theory exercise])))))))

(defrouter RootRouter [this {:keys [current-state] :as props}]
  {:router-targets     [Categories]}
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
