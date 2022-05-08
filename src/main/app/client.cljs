(ns app.client
  (:require
    [app.application :refer [APP]]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.data-fetch :as df]))

(defsc Content [this {:content/keys [id type text] :as props}]
  {:query [:content/id :content/type :content/text]
   :ident (fn [] [:content/id (:content/id props)])}
  (dom/div "Content" id))

(def ui-content (comp/factory Content))

(defsc CategoryHeader
  [this {:category/keys [id name href content] :ui/keys [first? last?]}]
  {:query [:category/id :category/name :category/href :category/first? :category/last? {:category/content (comp/get-query Content)}
           :ui/first? :ui/last?]
   :ident :category/id}
  (let [current? (= js/window.location.pathname href)]
    (dom/button {:key name
                 :onClick #(df/load! this [:category/id id] CategoryHeader {:focus [:category/content]})
                 ; :href href
                 :classes [(if current? "text-gray-900" "text-gray-500 hover:text-gray-700")
                           (when first? "rounded-l-lg")
                           (when last? "rounded-r-lg")
                           "group relative min-w-0 flex-1 overflow-hidden bg-white py-4 px-4 text-sm font-medium text-center hover:bg-gray-50 focus:z-10"]
                 :aria-label (if current? "page" nil)}
      (dom/span name)
      (dom/span {:aria-hidden "true" :classes [(if current? "bg-indigo-500" "bg-transparent") "absolute inset-x-0 bottom-0 h-0.5"]}))))

(def ui-category-header (comp/factory CategoryHeader))

(defsc Categories [this {:categories/keys [projects theory exercises] :as props}]
  {:query [{:categories/projects (comp/get-query CategoryHeader)}
           {:categories/theory (comp/get-query CategoryHeader)}
           {:categories/exercises (comp/get-query CategoryHeader)}]
   :initial-state (fn [_] {:categories/projects {:category/id :projects :category/name "Projects" :category/href "/projects" :category/content [] :ui/first? true :ui/last? false}
                           :categories/theory {:category/id :theory :category/name "Theory" :category/href "/theory" :category/content [] :ui/first? false :ui/last? false}
                           :categories/exercises {:category/id :exercises :category/name "Exercises" :category/href "/exercises" :category/content [] :ui/first? false :ui/last? true}})
   :ident (fn [] [:component/id ::Categories])}
  (dom/div
    (dom/nav {:classes ["relative z-0 rounded-lg shadow flex divide-x divide-gray-200"] :aria-label "Tabs"}
      (ui-category-header projects)
      (ui-category-header theory)
      (ui-category-header exercises))
    (map #(ui-content %) (:category/content projects))
    ))

(def ui-categories (comp/factory Categories))

(defsc Root [this {:keys [categories]}]
  {:query [{:categories (comp/get-query Categories)}]
   :initial-state (fn [p] {:categories (comp/get-initial-state Categories {:id ::Categories})})}
  (ui-categories categories))

(defn ^:export init
  "Shadow-cljs sets this up to be our entry-point function. See shadow-cljs.edn `:init-fn` in the modules of the main build."
  []
  (app/mount! APP Root "app")
  (js/console.log "Loaded"))

(defn ^:export refresh
  "During development, shadow-cljs will call this on every hot reload of source. See shadow-cljs.edn"
  []
  ;; re-mounting will cause forced UI refresh, update internals, etc.
  (app/mount! APP Root "app")
  ;; As of Fulcro 3.3.0, this addition will help with stale queries when using dynamic routing:
  (comp/refresh-dynamic-queries! APP)
  (js/console.log "Hot reload"))
