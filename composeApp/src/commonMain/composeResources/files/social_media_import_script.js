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
            platformSpecificImageURL = document.querySelector("video").parentElement.querySelector("img").src
        }
    }catch(e) {
        console.error(e)
    }

    const description = descriptionValues[0]
    const imageURL = platformSpecificImageURL || document.querySelector("meta[property='og:image']")?.content

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