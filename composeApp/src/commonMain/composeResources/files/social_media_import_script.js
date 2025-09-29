(()=>{
    const descriptionQuerySelector = [ "meta[property='og:description']", "meta[property='twitter:description']" ]
    const descriptionValues = []

    descriptionQuerySelector.forEach((s) => {
        let content = document.querySelector(s)?.content
        if(content !== undefined) descriptionValues.push(content)
    })

    if(descriptionValues.length == 0) return null

    let platformSpecificImageURL = undefined

    try {
        if(window.location.hostname.includes("tiktok")) {
            platformSpecificImageURL = document.querySelector("picture").querySelector("img").src
        }else if(window.location.hostname.includes("instagram")) {
            platformSpecificImageURL = document.getElementsByTagName("video")[0].parentElement.parentElement.parentElement.parentElement.querySelector("img").src
        }

        if(platformSpecificImageURL.length < 3) platformSpecificImageURL = undefined
    }catch(e) {
        console.error(e)
    }

    const description = descriptionValues[0]
    let imageURL = platformSpecificImageURL || document.querySelector("meta[property='og:image']")?.content

    if(imageURL.length < 3) imageURL = undefined

    // return different content when using WebKit
    if(/kitshnWebKit/.test(navigator.userAgent)) {
        return JSON.stringify({
            description: description,
            imageURL: imageURL
        })
    }else{
        return {
            description: description,
            imageURL: imageURL
        }
    }
})();