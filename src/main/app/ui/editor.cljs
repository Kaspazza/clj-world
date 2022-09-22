(ns app.ui.editor
  (:require
    ["@codemirror/language" :refer [foldGutter syntaxHighlighting defaultHighlightStyle indentNodeProp Language LanguageSupport]]
    ["@lezer/markdown" :as lezer-markdown]
    [app.application :refer [APP]]
    ["@codemirror/commands" :refer [history historyKeymap]]
    ["@codemirror/state" :refer [EditorState Prec]]
    ["@codemirror/view" :as view :refer [EditorView keymap lineNumbers]]
    ["@codemirror/lang-markdown" :as MD :refer [markdown markdownLanguage]]
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

(defn doc? [^js node] (== (.-Document lezer-markdown/parser.nodeTypes) (.. node -type -id)))

(defn handle-open-backticks [^js view]
  (let [state (.-state view)]
    (when (doc? (.-tree state))
      (let [sel (.. state -selection -main)]
        (when (and (.-empty sel)
                (identical? "``" (.. state -doc (lineAt (.-anchor sel)) -text)))
          (.dispatch view
            (.update state (j/lit {:changes [{:insert "\n```"
                                              :from (.-anchor sel)}]}))))))))

(def ^js markdown-language-support
  (let [^js md
        (markdown (j/obj :defaultCodeLanguage cm-clj/language-support
                    :base (Language.
                            (.-data markdownLanguage)
                            (.. markdownLanguage
                              -parser (configure
                                        ;; fixes indentation base for clojure inside fenced code blocks ⬇
                                        (j/lit {:props [(.add indentNodeProp
                                                          (j/obj :Document (constantly 0)))]}))))))]
    (LanguageSupport.
      (.-language md)
      (array (.-support md)
        (.high Prec (.of keymap (j/lit [{:key \` :run handle-open-backticks}])))))))

(defonce markdown-etensions #js[markdown-language-support])

(defonce extensions #js[theme
                        (history)
                        (syntaxHighlighting defaultHighlightStyle)
                        (view/drawSelection)
                        (lineNumbers)
                        (foldGutter)
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
                 :data-gramm "false"
                 :className "h-full"}
        (dom/div
          {:className "rounded-md h-full mb-0 text-sm monospace overflow-auto relative border shadow-lg bg-white"
           :ref editor-ref})))))

(def ui-editor (comp/factory Editor {:keyfn :editor/id}))
