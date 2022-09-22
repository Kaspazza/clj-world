(ns app.ui.lesson
  (:require

    [clojure.set :as set]
    [hickory.core :as hc]
    [camel-snake-kebab.core :as csk]
    [clojure.string :as str]
    [com.fulcrologic.fulcro.dom.html-entities :as ent]
    [clojure.pprint :refer [pprint]]
    [dompurify :as DOMPurify]
    [app.ui.editor :refer [Editor ui-editor]]
    [markdown.core :as md]
    [app.application :refer [APP]]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.data-fetch :as df]))

(def tabs [{:name "Description" :type :description}
           {:name "Solution" :type :solution}
           {:name "Cheatsheet" :type :cheatsheet}
           {:name "Editor" :type :editor}
           {:name "Notes" :type :notes}])

(def solution {:hints ["Check the last word twice "
                       "Do the backflip"
                       "Watcha gonna do"]
               :solution "(defn fizz-buzz [] 15)"
               :explanation "https://www.youtube.com/awe54123"})

(defn description-component [desc]
  (dom/div
           {:className "md-output w-full h-full"
            :dangerouslySetInnerHTML {:__html (DOMPurify/sanitize (md/md->html desc))}}))


(defn lesson-tab-content [tab id editor desc]
  (dom/div {:className "px-4 py-5 sm:p-6 overflow-y-auto grow"}
    (case tab
      ;; Markdown text
      :notes (dom/div {:style {:height "80vh"}}
                 #_(test-component))
      ;(ui-editor (comp/computed editor {:lesson-id id}))
      ;; map with :hints vector, :solution string and :explanation
      :solution (dom/div "Hint 1/Hint2/Solution Code/Explanatory Video")
      ;; Makrdown text
      :cheatsheet (dom/div "cheatsheet")
      ;; Markdown text
      :description (dom/div {:className "h-full"} (description-component desc))
      ;; Code editor
      :editor (ui-editor (comp/computed editor {:lesson-id id}))
      (dom/div "choose a tab"))))

(defn lesson-tabs [lesson-id current-tab]
  (dom/div
    (dom/div {:className "sm:hidden"}
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
                       :onClick (fn [_]
                                  (comp/transact! APP
                                    `[(app.mutations/change-editor-tab {:tab-type ~(:type tab)
                                                                        :lesson-id ~lesson-id})]))
                       :classes [(if (= current-tab (:type tab)) "bg-indigo-100 text-indigo-700" "text-gray-500 hover:text-gray-700")
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

(defsc Lesson [_ {:ui/keys [repl-state tab]
                  :lesson/keys [id full-desc desc title type editor]}]
  {:query [:lesson/id :lesson/desc :lesson/full-desc :lesson/title :lesson/type {:lesson/editor (comp/get-query Editor)}
           :ui/repl-state :ui/tab]
   :route-segment ["categories" :category-id :lesson-id]
   :will-enter (fn [app {:keys [lesson-id] :as props}]
                 (df/load! app [:lesson/id (uuid lesson-id)] Lesson)
                 (dr/route-immediate [:lesson/id (uuid lesson-id)]))
   :initial-state {:ui/repl-state []
                   :ui/tab :editor}
   :pre-merge (fn [env]
                (merge
                  (comp/get-initial-state Lesson)
                  (:current-normalized env)
                  (:data-tree env)))
   :ident :lesson/id}
  (dom/div {:className "h-full flex red"}
    (dom/div {:className "h-full flex-1 flex items-stretch overflow-hidden"}

      (dom/main {:className "flex-1 overflow-y-auto h-full"}
        (dom/div {:className "min-w-0 flex-1 h-full flex flex-col lg:order-last"}
          (dom/div {:className "bg-white overflow-hidden shadow rounded-lg divide-y divide-gray-200 h-full flex flex-col"}
            (dom/div {:className "px-4 py-5 sm:px-6"}
              (lesson-tabs id tab))
            (lesson-tab-content tab id editor full-desc))))

      (dom/aside {:className "hidden w-96 bg-white border-l border-gray-200 overflow-y-auto lg:block"}
        (repl-view repl-state)))))
