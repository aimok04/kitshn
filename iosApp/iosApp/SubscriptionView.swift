import SwiftUICore
import SwiftUI
import StoreKit
import ComposeApp
import SafariServices

struct SubscriptionView: View {
    @State private var showPurchaseAlert = false
    @State private var showFundingSheet = false
    @State private var showPPSheet = false
    @State private var showEulaSheet = false
    
    var body: some View {
        VStack {
            if #available(iOS 17.0, *) {
                SubscriptionStoreView(groupID: SUPPORT_SUBSCRIPTION_GROUP_ID)
                    .onInAppPurchaseCompletion { product, result in
                        if case .success(.success(let transaction)) = result {
                            showPurchaseAlert = true
                            MainKt.handleSubscriptionChange(isSubscribed: true)
                        }
                    }
                
                Button(MainKt.lang(key: "common_learn_more")) {
                    showFundingSheet = true
                }
                
                Button("Privacy policy") {
                    showPPSheet = true
                }
                
                Button("End-user license agreement") {
                    showEulaSheet = true
                }
            }
        }
        .alert(MainKt.lang(key: "ios_support_thanks"), isPresented: $showPurchaseAlert) {
            Button(MainKt.lang(key: "common_done"), role: .cancel) { }
        }
        .sheet(isPresented: $showFundingSheet) {
            SafariWebView(url: URL(string: "https://kitshn.app/funding")!)
        }
        .sheet(isPresented: $showPPSheet) {
            SafariWebView(url: URL(string: "https://gist.github.com/aimok04/cfe30838491c1f2eeddb8b2b9e9ba894")!)
        }
        .sheet(isPresented: $showEulaSheet) {
            SafariWebView(url: URL(string: "https://www.apple.com/legal/internet-services/itunes/dev/stdeula/")!)
        }
    }
}

struct SafariWebView: UIViewControllerRepresentable {
    let url: URL
    
    func makeUIViewController(context: Context) -> some UIViewController {
        return SFSafariViewController(url: url)
    }
    
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {
        
    }
}
