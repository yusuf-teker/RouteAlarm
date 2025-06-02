import SwiftUI
import GoogleMaps
import GooglePlaces
import UserNotifications

@main
struct iOSApp: App {
    init(){
        requestNotificationPermission()

        if let apiKey = Bundle.main.object(forInfoDictionaryKey: "GOOGLE_API_KEY") as? String {
        GMSServices.provideAPIKey(apiKey)
        GMSPlacesClient.provideAPIKey(apiKey)
    } else {
        fatalError("Google API key not found in Info.plist")
    }

    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

func requestNotificationPermission() {
    UNUserNotificationCenter
        .current()
        .requestAuthorization(options: [.alert, .badge]) { (success, error) in
            if success {
                print("Permission granted.")
            } else if let error = error {
                print(error.localizedDescription)
            }
    }
}
