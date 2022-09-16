(ns app.ui.editor
  (:require
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
    [nextjournal.clojure-mode.test-utils :as test-utils]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]))

(def editor-view-state (atom nil))

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

(defn editor-ref-fn [state]
  (fn [el]
    (when el
      (let [lesson-id (:lesson-id (:fulcro.client.primitives/computed state))
            editor-text (:editor/text state)

            update-repl-transaction (fn [result evaluated-line]
                                      (comp/transact! APP
                                        `[(app.mutations/update-repl-state {:repl-value ~result
                                                                            :lesson-id ~lesson-id
                                                                            :evaluated-line ~evaluated-line})]))

            on-evaluate-fn (fn [evaluated-line result] (update-repl-transaction result evaluated-line))

            obj-state (test-utils/make-state
                        (-> #js [extensions]
                          (.concat #js [(sci/extension {:modifier "Alt"
                                                        :on-result on-evaluate-fn})]))
                        editor-text)

            editor-view-obj (new EditorView
                              (j/obj
                                :state obj-state
                                :parent el))]
        (reset! editor-view-state editor-view-obj)))))

(defsc Editor [this {:editor/keys [id text]} {:keys [lesson-id]}]
  {:ident :editor/id
   :query [:editor/id :editor/text]
   :initLocalState (fn [_ state]
                     (when state
                       {:editor-ref (editor-ref-fn state)}))
   :componentWillUnmount (fn [_]
                           (some-> @editor-view-state
                             (j/call :destroy)))}
  (when id
    (let [editor-ref (comp/get-state this :editor-ref)]
      (dom/form {:spellCheck "false"
                 :data-gramm "false"}
        (dom/div
          {:className "rounded-md mb-0 text-sm monospace overflow-auto relative border shadow-lg bg-white"
           :ref editor-ref
           :style {:height 950}})))))

(def ui-editor (comp/factory Editor {:keyfn :editor/id}))
