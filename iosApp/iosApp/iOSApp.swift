import SwiftUI
import GoogleMaps

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        GMSServices.provideAPIKey("AIzaSyCi8hSsM3UBBcbJcEwWnoIwRNpJuzWcGFE")
        return true
    }
}
