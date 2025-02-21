import UIKit
import SwiftUI
import Bugsnag
import ComposeApp
import StoreKit

let KEY_ALLOW_CRASH_REPORTING = "allow_crash_reporting"
let KEY_CRASH_REPORTING_DIALOG_SHOWN = "crash_reporting_dialog_shown"

let SUPPORT_SUBSCRIPTION_GROUP_ID = "21623970"

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        if #available(iOS 17.0, *) {
            Task {
                for await result in Transaction.currentEntitlements {
                    guard case .verified(let transaction) = result else {
                        continue
                    }
                    
                    if(transaction.subscriptionGroupID == SUPPORT_SUBSCRIPTION_GROUP_ID) {
                        print("kitshn/iOS: User has support subscription.")
                        MainKt.handleSubscriptionChange(isSubscribed: true)
                    }
                }
            }
        }else{
            // everyone below iOS 17.0 is considered subscribed
            MainKt.handleSubscriptionChange(isSubscribed: true)
        }
        
        window = UIWindow(frame: UIScreen.main.bounds)
        if let window = window {
            window.rootViewController = MainKt.MainViewController(subscriptionUI: {
                return UIHostingController(rootView: SubscriptionView())
            })
            window.makeKeyAndVisible()
            
            if(!UserDefaults.standard.bool(forKey: KEY_CRASH_REPORTING_DIALOG_SHOWN)) {
                let alert = UIAlertController(title: MainKt.lang(key: "bugsnag_dialog_title"), message: MainKt.lang(key: "bugsnag_dialog_message"), preferredStyle: .alert)
                
                alert.addAction(UIAlertAction(title: MainKt.lang(key: "bugsnag_dialog_button_negative"), style: .default, handler: { [weak alert] (_) in
                    self.crashReportChoice(allow: false)
                }))

                alert.addAction(UIAlertAction(title: MainKt.lang(key: "bugsnag_dialog_button_positive"), style: .default, handler: { [weak alert] (_) in
                    self.crashReportChoice(allow: true)
                }))
                                              
                window.rootViewController?.present(alert, animated: true, completion: nil)
            }else{
                if(UserDefaults.standard.bool(forKey: KEY_ALLOW_CRASH_REPORTING)) {
                    print("kitshn/iOS: User allowed crash reporting.")
                    self.startCrashReporting()
                }else{
                    print("kitshn/iOS: User denied crash reporting.")
                }
            }
        }
        return true
    }
    
    func application(
        _ application: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey : Any] = [:]
    ) -> Bool {
        MainKt.handleDeepLink(
            url: url.absoluteString.removingPercentEncoding
        )
        return true
    }
    
    func application(
        _ application: UIApplication,
        continue userActivity: NSUserActivity,
        restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void
    ) -> Bool {
        if(userActivity.activityType == NSUserActivityTypeBrowsingWeb) {
            MainKt.handleDeepLink(
                url: userActivity.webpageURL?.absoluteString
            )
            return true
        }

        return false
    }
    
    func crashReportChoice(allow: Bool) {
        UserDefaults.standard.set(allow, forKey: KEY_ALLOW_CRASH_REPORTING)
        UserDefaults.standard.set(true, forKey: KEY_CRASH_REPORTING_DIALOG_SHOWN)
        if(allow) { startCrashReporting() }
    }
    
    func startCrashReporting() {
        print("kitshn/iOS: Starting Bugsnag ...")

        let config = BugsnagConfiguration.loadConfig()
        BugsnagConfigKt.startBugsnag(config: config)
    }
}
