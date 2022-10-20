(ns other.landing-page
  (:require [ring.middleware.params :refer [wrap-params]]
            [hiccup.page :as h.page]
            [postal.core :as pc]))

(def email (atom ""))

(defn successful-email []
  [:div.rounded-md.bg-green-50.p-4.absolute.w-full
   [:div.flex.justify-between
    [:div
     [:svg.h-5.w-5.text-green-400 {:xmlns "http://www.w3.org/2000/svg" :viewBox "0 0 20 20" :fill "currentColor" :aria-hidden "true"}
      [:path {:fill-rule "evenodd" :d "M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" :clip-rule "evenodd"}]]]
    [:div
     [:p.text-sm.font-medium.text-green-800 "Thanks for signing up for release!"]]
    [:div
     [:div.-mx-1.5.-my-1.5]]]])

(defn landing-page []
  (h.page/html5 {}
                [:head {:lang "en"}
                 [:meta {:charset "UTF-8"}]
                 [:link {:href "/dist/output.css" :rel "stylesheet"}]
                 [:link {:href "favicon.ico" :rel "shortcut icon"}]]
                [:body
                 (when (seq @email) (successful-email))
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
                       [:p.mx-auto.mt-6.max-w-2xl.text-lg.text-indigo-200 "Project-based interactive learning materials for clojure will be available soon."]
                       [:p.mx-auto.mt-6.max-w-2xl.text-lg.text-indigo-200 "Be the first one to know when it's ready!"]]
                      [:form.mt-12.sm:mx-auto.sm:flex.sm:max-w-lg {:method "GET"}
                       [:div.min-w-0.flex-1
                        [:label.sr-only {:for "cta-email"} "Email address"]
                        [:input#cta-email.block.w-full.rounded-md.border.border-transparent.px-5.py-3.text-base.text-gray-900.placeholder-gray-500.shadow-sm.focus:border-transparent.focus:outline-none.focus:ring-2.focus:ring-white.focus:ring-offset-2.focus:ring-offset-indigo-600
                         {:type        "email"
                          :value       @email
                          :name        "cta-email"
                          :placeholder "Enter your email"}]]

                       [:div.mt-4.sm:mt-0.sm:ml-3
                        [:button.block.w-full.rounded-md.border.border-transparent.bg-indigo-500.px-5.py-3.text-base.font-medium.text-white.shadow.hover:bg-indigo-400.focus:outline-none.focus:ring-2.focus:ring-white.focus:ring-offset-2.focus:ring-offset-indigo-600.sm:px-10
                         {:type "submit"} "Notify me"]]]]]]]]]))

(defn generate-landing-page [req]
  (if-let [user-email (get (:params ((wrap-params identity) req)) "cta-email")]
    (do (pc/send-message
      {:host "smtp.gmail.com"
       :user (System/getenv "EMAIL_USER")
       :pass (System/getenv "EMAIL_PASS")
       :port 587
       :tls  true}
      {:from    "kaspazza@gmail.com"
       :to      "kaspazza@gmail.com"
       :subject "clojuretutorials.com waitlist"
       :body    user-email})
    (reset! email user-email))
    (reset! email ""))
  (landing-page))