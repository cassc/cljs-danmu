(ns danmu.core
  (:require
   [reanimated.core :as anim]
   [clojure.string :as s]
   [alandipert.storage-atom :refer [local-storage]]
   [reagent.core :as reagent :refer [atom]]
   [cljs.core.async :refer [<! chan sliding-buffer put! close! timeout alts!]])
  (:require-macros
   [cljs.core.async.macros :refer [go-loop go]]))

(enable-console-print!)

(defonce dm-items (local-storage (atom []) :dm-items))
(defonce displayed-items (atom []))
(defonce msg-store (atom nil))

(defn rand-key []
  (str (.random js/Math)))

(defn rand-time
  "this controls the speed of danmu"
  []
  (+ 5 (rand-int 40)))

(defn rand-top []
  (rand-nth (range 10 280)))

(defn gen-animate-item [item]
  {:item item
   :key (rand-key)
   :color (str "rgb(" (rand-int 255) "," (rand-int 255) "," (rand-int 255) ")")
   :top (atom (rand-top))
   :left (atom 0)
   :time (atom (rand-time))})

(defn load-as-displayed-items! []
  (reset! displayed-items (mapv gen-animate-item @dm-items)))

(defn add-item []
  (when-let [nitem (and (not (s/blank? @msg-store)) @msg-store)]
    (swap! dm-items conj nitem)
    (swap! displayed-items conj (gen-animate-item nitem))
    (reset! msg-store nil)))

(defn clear-all! []
  (reset! displayed-items [])
  (reset! dm-items []))

(defn item-exist? [key]
  (some #(= (:key %) key) @displayed-items))

(defn animated-item [{:keys [item time top color left top key]}]
  (let [update-x (fn [x] (let [nx (inc x)]
                           (if (> nx 800)
                             (do
                               (reset! time (rand-time))
                               (reset! top (rand-top))
                               0)
                             nx)))
        loop-start (atom true)
        looper (fn []
                 (go-loop []
                   (when (and @loop-start (item-exist? key))
                     (swap! left update-x)
                     (<! (timeout @time))
                     (recur))))]
    (reagent/create-class
     {:component-did-mount #(looper)
      :component-will-unmount #(reset! loop-start nil)
      :reagent-render (fn [{:keys [item time top color left top]}]
                        [:div.animated-item
                         [:svg
                          {:width 800
                           :height 300
                           :fill color}
                          ;;[anim/interval #(swap! left update-x) @time]
                          [:svg
                           [:text {:x @left :y @top} item]]]])})))

(defn display-dm-items []
  [:div.board
   (for [{:keys [key] :as aitem} @displayed-items]
     ^{:key key}
     [animated-item aitem])])

(defn my-app []
  (fn []
    [:div.main-app
     [:div
      [display-dm-items]]
     [:div.row
      [:input.msg {:type :text :value @msg-store :on-change #(reset! msg-store (-> % .-target .-value))}]]
     [:div.row
      [:button {:type :button :on-click add-item} "Send"]
      [:button {:type :button :on-click clear-all!} "Clear"]]]))

(defn main []
  (load-as-displayed-items!)
  (reagent/render [#'my-app] (.getElementById js/document "app")))

(main)

