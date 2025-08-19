(()=>{
    let description = ""
    let imageURL = ""

    if(document.querySelector("meta[property='og:description']") != null) {
        description = document.querySelector("meta[property='og:description']").content
    }else if(document.querySelector("meta[property='twitter:description']")) {
        description = document.querySelector("meta[property='twitter:description']").content
    }

    if(document.querySelector("meta[property='og:image']") != null) {
        imageURL = document.querySelector("meta[property='og:image']").content
    }

    try {
        if(window.location.hostname.includes("tiktok"))
                imageURL = document.getElementsByTagName("video")[0].parentElement.parentElement.parentElement.parentElement.querySelector("img").src
    }catch(e) {
        console.error(e)
    }

    if(PLATFORM == "IOS") {
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