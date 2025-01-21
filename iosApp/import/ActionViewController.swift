import UIKit
import MobileCoreServices
import UniformTypeIdentifiers

class ActionViewController: UIViewController {

    func redirectToApp(data: String) {
        let urlString = "kitshn://import/" + data.addingPercentEncoding(withAllowedCharacters: CharacterSet())!
        guard let redirectionURL = URL(string: urlString) else {
            return
        }
        openURL(redirectionURL)
    }

    @objc @discardableResult func openURL(_ url: URL) -> Bool {
        var responder: UIResponder? = self
        while responder != nil {
            if let application = responder as? UIApplication {
                if #available(iOS 18.0, *) {
                    application.open(url, options: [:], completionHandler: nil)
                    return true
                } else {
                    return application.perform(#selector(openURL(_:)), with: url) != nil
                }
            }
            responder = responder?.next
        }
        return false
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        for textItem in self.extensionContext!.inputItems {
            let mTextItem = textItem as? NSExtensionItem
            if(mTextItem != nil) {
                for textItemAttachment in mTextItem!.attachments! {
                    let provider = textItemAttachment as? NSItemProvider
                    if(provider != nil) {
                        if provider!.hasItemConformingToTypeIdentifier(kUTTypeURL as String) {
                            provider!.loadItem(
                                forTypeIdentifier: kUTTypeURL as String,
                                options: nil,
                                completionHandler: { (result, error) in
                                    let str = result as? URL
                                    if str != nil {
                                        self.redirectToApp(data: str!.absoluteString)
                                        self.done()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    @IBAction func done() {
        self.extensionContext!.completeRequest(returningItems: self.extensionContext!.inputItems, completionHandler: nil)
    }

}
