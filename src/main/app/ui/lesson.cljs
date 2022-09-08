(ns app.ui.lesson
  (:require
    ["@codemirror/closebrackets" :refer [closeBrackets]]
    ["@codemirror/fold" :as fold]
    [app.application :refer [APP]]
    ["@codemirror/gutter" :refer [lineNumbers]]
    ["@codemirror/highlight" :as highlight]
    ["@codemirror/history" :refer [history historyKeymap]]
    ["@codemirror/state" :refer [EditorState]]
    ["@codemirror/view" :as view :refer [EditorView]]
    [applied-science.js-interop :as j]
    [app.code-mirror.sci :as sci]
    [nextjournal.clojure-mode :as cm-clj]
    [nextjournal.clojure-mode.extensions.close-brackets :as close-brackets]
    [nextjournal.clojure-mode.extensions.formatting :as format]
    [nextjournal.clojure-mode.extensions.selection-history :as sel-history]
    [nextjournal.clojure-mode.keymap :as keymap]
    [nextjournal.clojure-mode.node :as n]
    [nextjournal.clojure-mode.selections :as sel]
    [nextjournal.clojure-mode.test-utils :as test-utils]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.data-fetch :as df]))

(declare Lesson)

(def theme
  (.theme EditorView
    (j/lit {".cm-content" {:white-space "pre-wrap"
                           :padding "10px 0"}
            "&.cm-focused" {:outline "none"}
            ".cm-line" {:padding "0 9px"
                        :line-height "1.6"
                        :font-size "16px"}
            ".cm-matchingBracket" {:border-bottom "1px solid green"
                                   :color "inherit"}
            ".cm-gutters" {:background "transparent"
                           :border "none"}
            ".cm-gutterElement" {:margin-left "5px"}
            ;; only show cursor when focused
            ".cm-cursor" {:visibility "visible"}
            "&.cm-focused .cm-cursor" {:visibility "visible"}
            })))

(defonce extensions #js[theme
                        (history)
                        highlight/defaultHighlightStyle
                        (view/drawSelection)
                        (lineNumbers)
                        (fold/foldGutter)
                        (.. EditorState -allowMultipleSelections (of true))
                        cm-clj/default-extensions
                        (.of view/keymap cm-clj/complete-keymap)
                        (.of view/keymap historyKeymap)])

(def tabs [{:name "Description" :current false}
           {:name "Solution" :current false}
           {:name "Cheatsheet" :current false}
           {:name "Editor" :current true}])

(defn editor-tabs []
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


(def editor-view-state (atom nil))

(defsc Editor [this {:editor/keys [id text]
                     :ui/keys [editor-obj]} {:keys [content-id]}]
  {:ident :editor/id
   :query [:editor/id :editor/text :ui/editor-obj]
   :initLocalState (fn [this state]
                     (when state
                       {:editor-ref (fn [el]
                                      (when el
                                        (reset! editor-view-state (new EditorView
                                                                    (j/obj :state
                                                                      (test-utils/make-state
                                                                        (cond-> #js [extensions]
                                                                          true (.concat #js [(sci/extension {:modifier "Alt"
                                                                                                             :on-result (fn [evaluated-line result]
                                                                                                                          (comp/transact! APP
                                                                                                                            `[(app.mutations/update-repl-state {:repl-value ~result
                                                                                                                                                                :content-id ~(:content-id (:fulcro.client.primitives/computed state))
                                                                                                                                                                :evaluated-line ~evaluated-line})]))})])) (:editor/text state))
                                                                      :parent el)))))}))
   :componentWillUnmount (fn [this]
                           (some-> @editor-view-state
                             (j/call :destroy)))
   }
  (when id
    (let [editor-ref (comp/get-state this :editor-ref)]
      (dom/form {:spellCheck "false"
                 :data-gramm "false"}
        (dom/div
          {:className "rounded-md mb-0 text-sm monospace overflow-auto relative border shadow-lg bg-white"
           :ref editor-ref
           :style {:height 950}})))))

(def ui-editor (comp/factory Editor {:keyfn :editor/id}))

(defsc Lesson [this {:ui/keys [repl-state]
                     :content/keys [id desc title type editor]
                     :as props}]
  {:query [:content/id :content/desc :content/title :content/type :ui/repl-state {:content/editor (comp/get-query Editor)}]
   :route-segment ["categories" :category-id :content-id]
   :will-enter (fn [_app {:keys [content-id] :as props}]
                 (df/load! APP [:content/id (uuid content-id)] Lesson)
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
        (dom/div
          {:className "min-w-0 flex-1 h-full flex flex-col lg:order-last"}
          (dom/div {:className "bg-white overflow-hidden shadow rounded-lg divide-y divide-gray-200"}
            (dom/div {:className "px-4 py-5 sm:px-6"}
              (editor-tabs))
            (dom/div {:className "px-4 py-5 sm:p-6 min-h-full"}
              (ui-editor (comp/computed editor {:content-id id}))))))

      (dom/aside {:className "hidden w-96 bg-white border-l border-gray-200 overflow-y-auto lg:block"}
        (if (seq repl-state)
          (dom/div
            (map (fn [repl-value]
                   (dom/div
                     (dom/div
                       (dom/div (:line repl-value))
                       (dom/div " -> ")
                       (dom/div (:value repl-value)))
                     (dom/hr))) repl-state))

          "Hi, I'm your REPL, feel free to use me!")))))
