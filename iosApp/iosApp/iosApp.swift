import UIKit
import Bugsnag
import ComposeApp

let KEY_ALLOW_CRASH_REPORTING = "allow_crash_reporting"
let KEY_CRASH_REPORTING_DIALOG_SHOWN = "crash_reporting_dialog_shown"

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        window = UIWindow(frame: UIScreen.main.bounds)
        if let window = window {
            window.rootViewController = MainKt.MainViewController()
            window.makeKeyAndVisible()
            
            if(!UserDefaults.standard.bool(forKey: KEY_CRASH_REPORTING_DIALOG_SHOWN)) {
                let alert = UIAlertController(title: "Allow crash reporting?", message: "kitshn for iOS depends on crash reporting to be improved. The app uses the Bugsnag library/platform to accomplish that. Are you fine with automatically uploading details about crashes and bugs?\n\nThis choice can be changed in Settings/Behavior.", preferredStyle: .alert)
                
                alert.addAction(UIAlertAction(title: "No, don't allow", style: .default, handler: { [weak alert] (_) in
                    self.crashReportChoice(allow: false)
                }))

                alert.addAction(UIAlertAction(title: "Yes, allow", style: .default, handler: { [weak alert] (_) in
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
