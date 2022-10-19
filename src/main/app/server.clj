(ns app.server
  (:require
    [app.parser :refer [api-parser]]
    [com.fulcrologic.fulcro.algorithms.denormalize :as fdn]
    [org.httpkit.server :as http]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.algorithms.server-render :as ssr]
    [hiccup.page :as h.page]
    [com.fulcrologic.fulcro.server.api-middleware :as server]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.not-modified :refer [wrap-not-modified]]
    [ring.middleware.session :refer [wrap-session]]
    [ring.middleware.resource :refer [wrap-resource]]
    [clojure.string :as str]
    [ring.util.response :as resp]
    [app.ui.landing-page :refer [LandingPage]]
    [com.fulcrologic.fulcro.dom-server :as dom]))

(defn generate-index []
  (h.page/html5 {}
                [:head {:lang "en"}
                 [:meta {:charset "UTF-8"}]
                 [:link {:href "/dist/output.css" :rel "stylesheet"}]
                 [:link {:href "/css/styles.css" :rel "stylesheet"}]
                 [:link {:href "/css/general.css" :rel "stylesheet"}]
                 [:link {:href "/css/markdown.css" :rel "stylesheet"}]
                 [:link {:href "favicon.ico" :rel "shortcut icon"}]]
                [:body
                 [:div {:id "app"}]
                 [:script {:src "/js/main/main.js"}]]))

(defn generate-landing-page []
  (h.page/html5 {}
                [:head {:lang "en"}
                 [:meta {:charset "UTF-8"}]
                 [:link {:href "/dist/output.css" :rel "stylesheet"}]
                 [:link {:href "favicon.ico" :rel "shortcut icon"}]]
                [:body
                 [:div.bg-white.py-16.sm:py-24
                  [:div.relative.sm:py-16
                   [:div.hidden.sm:block {:aria-hidden "true"}
                    [:div.absolute.inset-y-0.left-0.rounded-r-3xl.bg-gray-50 {:class "w-1/2"}]
                    [:svg.absolute.top-8.-ml-3 {:class "left-1/2" :width "404" :height "392" :fill "none" :viewBox "0 0 404 392"}
                     [:defs
                      [:pattern#8228f071-bcee-4ec8-905a-2a059a2cc4fb {:x "0" :y "0" :width "20" :height "20" :patternUnits "userSpaceOnUse"}
                       [:rect.text-gray-200 {:x "0" :y "0" :width "4" :height "4" :fill "currentColor"}]]]
                     [:rect {:width "404" :height "392" :fill "url(#8228f071-bcee-4ec8-905a-2a059a2cc4fb)"}]]]
                   [:div.mx-auto.max-w-md.px-4.sm:max-w-3xl.sm:px-6.lg:max-w-7xl.lg:px-8
                    [:div.relative.overflow-hidden.rounded-2xl.bg-indigo-600.px-6.py-10.shadow-xl.sm:px-12.sm:py-20
                     [:div.absolute.inset-0.-mt-72.sm:-mt-32.md:mt-0 {:aria-hidden "true"}
                      [:svg.absolute.inset-0.h-full.w-full {:preserveAspectRatio "xMidYMid slice" :xmlns "http://www.w3.org/2000/svg" :fill "none" :viewBox "0 0 1463 360"}
                       [:path.text-indigo-500.text-opacity-40 {:fill "currentColor" :d "M-82.673 72l1761.849 472.086-134.327 501.315-1761.85-472.086z"}]
                       [:path.text-indigo-700.text-opacity-40 {:fill "currentColor" :d "M-217.088 544.086L1544.761 72l134.327 501.316-1761.849 472.086z"}]]]
                     [:div.relative
                      [:div.sm:text-center
                       [:h2.text-3xl.font-bold.tracking-tight.text-white.sm:text-4xl "Get notified when we&rsquo;re launching."]
                       [:p.mx-auto.mt-6.max-w-2xl.text-lg.text-indigo-200 "Sagittis scelerisque nulla cursus in enim consectetur quam. Dictum urna sed consectetur neque tristique pellentesque."]]
                      [:form.mt-12.sm:mx-auto.sm:flex.sm:max-w-lg {:action "#"}
                       [:div.min-w-0.flex-1
                        [:label.sr-only {:for "cta-email"} "Email address"]
                        [:input#cta-email.block.w-full.rounded-md.border.border-transparent.px-5.py-3.text-base.text-gray-900.placeholder-gray-500.shadow-sm.focus:border-transparent.focus:outline-none.focus:ring-2.focus:ring-white.focus:ring-offset-2.focus:ring-offset-indigo-600 {:type "email" :placeholder "Enter your email"}]]
                       [:div.mt-4.sm:mt-0.sm:ml-3
                        [:button.block.w-full.rounded-md.border.border-transparent.bg-indigo-500.px-5.py-3.text-base.font-medium.text-white.shadow.hover:bg-indigo-400.focus:outline-none.focus:ring-2.focus:ring-white.focus:ring-offset-2.focus:ring-offset-indigo-600.sm:px-10 {:type "submit"} "Notify me"]]]]]]]]]))

(def not-found-handler
  (fn [_req]
    {:status 404
     :body {}}))

(defn wrap-html-routes [ring-handler]
  (fn [{:keys [uri] :as req}]
    (if (or (str/starts-with? uri "/api")
            (str/starts-with? uri "/images")
            (str/starts-with? uri "/files")
            (str/starts-with? uri "/js")
            (str/starts-with? uri "/dist")
            (str/starts-with? uri "/css"))
      (ring-handler req)

      (-> (resp/response (if (= uri "/") (generate-landing-page) (generate-index)))
          (resp/content-type "text/html"))
      )))

(def middleware
  (-> not-found-handler
      (server/wrap-api {:uri "/api"
                        :parser api-parser})
      (server/wrap-transit-params {})
      (server/wrap-transit-response {})
      (wrap-session)
      (wrap-resource "public")
      wrap-content-type
      wrap-not-modified
      (wrap-html-routes)))

(defonce stop-fn (atom nil))

(defn start []
  (reset! stop-fn (http/run-server middleware {:port 3000})))

(defn stop []
  (when @stop-fn
    (@stop-fn)
    (reset! stop-fn nil)))
