(ns app.ui.categories
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
            [app.application :refer [APP]]))

(defsc Content [this {:content/keys [id type title desc] :as props}]
  {:query [:content/id :content/type :content/title :content/desc]
   :initial-state {}
   :ident (fn [] [:content/id (:content/id props)])}
  (dom/li {:className "relative"}
    (dom/div {:className "group block w-full aspect-w-10 aspect-h-7 rounded-lg bg-gray-100
                          focus-within:ring-2 focus-within:ring-offset-2 focus-within:ring-offset-gray-100
                          focus-within:ring-indigo-500 overflow-hidden"}
      (dom/img {:src "https://techcrunch.com/wp-content/uploads/2015/04/codecode.jpg?w=1390&crop=1"
                :alt "Lesson picture"
                :className "object-cover pointer-events-none group-hover:opacity-75"})
      (dom/button {:type "button"
                   :className "absolute inset-0 focus:outline-none"
                   :onClick (fn [_e]
                              (comp/transact! this `[(app.mutations/open-lesson {:category-id ~(name type)
                                                                                 :content-id ~id})]))}
        (dom/span {:className "sr-only"} "View details for " title)))
    (dom/p {:className "mt-2 block text-sm font-medium text-gray-900 truncate pointer-events-none"} title)
    (dom/p {:className "block text-sm font-medium text-gray-500 pointer-events-none"} desc)))

(def ui-content (comp/factory Content {:keyfn :content/id}))

(defn load-lessons! [this]
  (let [active? (:ui/active? (comp/props this))
        category-id (:category/id (comp/props this))
        content (:category/content (comp/props this))]
    (when (and active? (empty? content))
      (comp/transact! this `[(app.mutations/load-category-lessons {:chosen-id ~category-id})]))))

(defsc CategoryHeader
  [this {:category/keys [id content] category-name :category/name :ui/keys [first? last? active?]}]
  {:query [:category/id :category/name {:category/content (comp/get-query Content)} :ui/first? :ui/last? :ui/active?]
   :componentDidMount (fn [this]
                        (load-lessons! this))
   :componentDidUpdate (fn [this]
                         (load-lessons! this))
   :initial-state (fn [{:category/keys [id content]
                        category-name :category/name
                        :ui/keys [first? last? active?]}]
                    {:category/id id
                     :category/name category-name
                     :category/content content
                     :ui/first? first?
                     :ui/last? last?
                     :ui/active active?})
   :ident :category/id}
  (dom/button {:key category-name
               :onClick (fn [_e]
                          (comp/transact! this `[(app.mutations/change-category {:chosen-id ~id})]))
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
   :route-segment ["categories" :category-id]
   :will-enter (fn [_app {:keys [category-id]}]
                 (comp/transact! APP `[(app.mutations/change-active-tab {:chosen-id ~category-id})])
                 (dr/route-immediate [:component/id ::Categories]))
   :initial-state (fn [_] {:categories/project (comp/get-initial-state CategoryHeader {:category/id :project :category/name "Projects" :category/content [] :ui/first? true :ui/last? false :ui/active? false})
                           :categories/theory (comp/get-initial-state CategoryHeader {:category/id :theory :category/name "Theory" :category/content [] :ui/first? false :ui/last? false :ui/active? false})
                           :categories/exercise (comp/get-initial-state CategoryHeader {:category/id :exercise :category/name "Exercises" :category/content [] :ui/first? false :ui/last? true :ui/active? false})})
   :ident (fn [] [:component/id ::Categories])}
  (dom/div
    (dom/nav {:classes ["relative z-0 rounded-lg shadow flex divide-x divide-gray-200"] :aria-label "Tabs"}
      (ui-category-header project)
      (ui-category-header theory)
      (ui-category-header exercise))
    (dom/ul {:role "list" :className "grid grid-cols-2 gap-x-4 gap-y-8 sm:grid-cols-3 sm:gap-x-6 lg:grid-cols-4 xl:gap-x-8 mt-4 mx-4"}
      (map #(ui-content %)
        (:category/content (first
                             (filter (fn [item]
                                       (:ui/active? item))
                               [project theory exercise])))))))
