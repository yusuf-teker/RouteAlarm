import SwiftUI
import GoogleMaps
import GooglePlaces


@main
struct iOSApp: App {
    init(){
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
