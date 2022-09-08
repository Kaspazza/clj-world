(ns app.ui.lesson
  (:require
    [app.ui.editor :refer [Editor ui-editor]]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.data-fetch :as df]))

(def tabs [{:name "Description" :current false}
           {:name "Solution" :current false}
           {:name "Cheatsheet" :current false}
           {:name "Editor" :current true}])

(defn lesson-view-tabs []
  (dom/div
    (dom/div
      {:className "sm:hidden"}
      (dom/label {:htmlFor "tabs", :className "sr-only"} "Select a tab")
      (dom/select
        {:id "tabs",
         :name "tabs",
         :className "block w-full focus:ring-indigo-500 focus:border-indigo-500 border-gray-300 rounded-md",
         :defaultValue (fn [] (prn "{tabs.find((tab) => tab.current).name}"))}
        (map (fn [tab]
               (dom/option {:key (str (:name tab) ">" (:name tab))})) tabs)))
    (dom/div
      {:className "hidden sm:block"}
      (dom/nav
        {:className "flex space-x-4", :aria-label "Tabs"}

        (map (fn [tab]
               (dom/a {:key (:name tab)
                       :classes [(if (:current tab) "bg-indigo-100 text-indigo-700" "text-gray-500 hover:text-gray-700")
                                 "px-3 py-2 font-medium text-sm rounded-md cursor-pointer"]}
                 (:name tab)))
          tabs)))))

(defn repl-view [repl-state]
  (if (seq repl-state)
    (dom/div
      (map (fn [repl-value]
             (dom/div
               (dom/div
                 (dom/div (:line repl-value))
                 (dom/div " -> ")
                 (dom/div (:value repl-value)))
               (dom/hr))) repl-state))

    (dom/div "Hi, I'm your REPL, feel free to use me!")))

(defsc Lesson [_ {:ui/keys [repl-state]
                  :content/keys [id desc title type editor]}]
  {:query [:content/id :content/desc :content/title :content/type :ui/repl-state {:content/editor (comp/get-query Editor)}]
   :route-segment ["categories" :category-id :content-id]
   :will-enter (fn [app {:keys [content-id] :as props}]
                 (df/load! app [:content/id (uuid content-id)] Lesson)
                 (dr/route-immediate [:content/id (uuid content-id)]))
   :initial-state {:ui/repl-state []}
   :pre-merge (fn [env]
                (merge
                  (comp/get-initial-state Lesson)
                  (:current-normalized env)
                  (:data-tree env)))
   :ident :content/id}
  (dom/div {:className "h-full flex mt-5 red"}
    (dom/div {:className "flex-1 flex items-stretch overflow-hidden"}

      (dom/main {:className "flex-1 overflow-y-auto"}
        (dom/div {:className "min-w-0 flex-1 h-full flex flex-col lg:order-last"}
          (dom/div {:className "bg-white overflow-hidden shadow rounded-lg divide-y divide-gray-200"}
            (dom/div {:className "px-4 py-5 sm:px-6"}
              (lesson-view-tabs))
            (dom/div {:className "px-4 py-5 sm:p-6 min-h-full"}
              (ui-editor (comp/computed editor {:content-id id}))))))

      (dom/aside {:className "hidden w-96 bg-white border-l border-gray-200 overflow-y-auto lg:block"}
        (repl-view repl-state)))))
