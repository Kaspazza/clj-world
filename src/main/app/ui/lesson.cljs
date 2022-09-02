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
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]))

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

;(def last-result
;  (atom (sci/eval-string "hello")))

(def sample
  "
  (defn fizz-buzz [n]\n  (condp (fn [a b] (zero? (mod b a))) n\n    15 \"fizzbuzz\"\n    3  \"fizz\"\n    5  \"buzz\"\n    n))

  (comment
  (fizz-buzz 1)
  (fizz-buzz 3)
  (fizz-buzz 5)
  (fizz-buzz 15)
  (fizz-buzz 17)
  (fizz-buzz 42))")

;(defn editor [source {:keys [eval?]}]
;  (let [!view (atom nil)
;        last-result (when eval? (atom (sci/eval-string source)))
;        mount! (fn [el]
;                 (when el
;                   (reset! !view (new EditorView
;                                   (j/obj :state
;                                     (test-utils/make-state
;                                       (cond-> #js [extensions]
;                                         eval? (.concat #js [(sci/extension {:modifier "Alt"
;                                                                             :on-result (partial reset! last-result)})]))
;                                       source)
;                                     :parent el)))))]
;    [:div
;     [:div {:class "rounded-md mb-0 text-sm monospace overflow-auto relative border shadow-lg bg-white"
;            :ref mount!
;            :style {:max-height 410}}]
;     (when eval?
;       [:div.mt-3.mv-4.pl-6 {:style {:white-space "pre-wrap" :font-family "var(--code-font)"}}
;        (prn-str @last-result)])]
;    ;;THIS IS USED WITH reagent/with-let and called when component is destoryed, so I could turn editor into defsc
;    #_(finally
;        (j/call @!view :destroy))))

(defn simple-editor []
  (dom/form {:spellCheck "false"
             :data-gramm "false"}
    (dom/div
      {:className "rounded-md mb-0 text-sm monospace overflow-auto relative border shadow-lg bg-white"
       :ref (fn [el]
              (when el
                (new EditorView
                  (j/obj :state
                    (test-utils/make-state
                      (cond-> #js [extensions]
                        true (.concat #js [(sci/extension {:modifier "Alt"
                                                           :on-result (fn [result]
                                                                        (prn "result: " result)
                                                                        (comp/transact! APP
                                                                          `[(app.mutations/update-repl-state {:repl-value ~result})]))})])) sample)
                    :parent el))))
       :style {:height 950}})))


(def tabs [{:name "Description" :current true}
           {:name "Solution" :current false}
           {:name "Cheatsheet" :current false}
           {:name "Editor" :current false}])

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

(defsc Lesson [this {:ui/keys [active? repl-state]
                     :content/keys [id]
                     :as props}]
  {:query [:content/id :ui/active? :ui/repl-state]
   :route-segment ["categories" :category-id :content-id]
   :will-enter (fn [_app {:keys [content-id]}]
                 (dr/route-immediate [:content/id content-id]))
   :initial-state {}
   :ident :content/id}
  (dom/div {:className "h-full flex mt-5"}
    (prn "props: " props)
    (dom/div {:className "flex-1 flex items-stretch overflow-hidden"}
      (dom/main {:className "flex-1 overflow-y-auto"}
        (dom/div
          {:className "min-w-0 flex-1 h-full flex flex-col lg:order-last"}
          (dom/div {:className "bg-white overflow-hidden shadow rounded-lg divide-y divide-gray-200"}
            (dom/div {:className "px-4 py-5 sm:px-6"}
              (editor-tabs))
            (dom/div {:className "px-4 py-5 sm:p-6 min-h-full"}
              (simple-editor)))))

      (dom/aside {:className "hidden w-96 bg-white border-l border-gray-200 overflow-y-auto lg:block"}
        (if (seq repl-state) repl-state "Hi, I'm your REPL, feel free to use me!")))))

;(defn samples []
;  (into [:<>]
;    (for [source ["(comment
;  (fizz-buzz 1)
;  (fizz-buzz 3)
;  (fizz-buzz 5)
;  (fizz-buzz 15)
;  (fizz-buzz 17)
;  (fizz-buzz 42))
;(defn fizz-buzz [n]
;  (condp (fn [a b] (zero? (mod b a))) n
;    15 \"fizzbuzz\"
;    3  \"fizz\"
;    5  \"buzz\"
;    n))"]]
;      [editor source {:eval? true}])))