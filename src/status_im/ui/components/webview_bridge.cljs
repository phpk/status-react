(ns status-im.ui.components.webview-bridge
  (:require [reagent.core :as reagent]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [reagent.core :as reagent.core]
            [status-im.utils.platform :as platform]))

(def webview-bridge-class
  (reagent/adapt-react-class (.-default js-dependencies/webview-bridge)))

(def module (.-WebViewBridgeModule (.-NativeModules js-dependencies/react-native)))

(defn webview-bridge [{:keys [dapp? dapp-name] :as opts}]
  (if (and platform/android? (not dapp?))
    [webview-bridge-class opts]
    (reagent.core/create-class
     (let [dapp-name-sent? (reagent.core/atom false)]
       {:component-will-mount
        (fn []
          ;; unfortunately it's impossible to pass some initial params
          ;; to view, that's why we have to pass dapp-name to the module
          ;; before showing webview
          (.setCurrentDapp module dapp-name
                           (fn [] (reset! dapp-name-sent? true))))
        :reagent-render
        (fn [opts]
          (when @dapp-name-sent?
            [webview-bridge-class opts]))}))))
