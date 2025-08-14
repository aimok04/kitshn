(()=>{
    let description = document.querySelector("meta[property='og:description']").content
    let imageURL = document.querySelector("meta[property='og:image']").content

    if(window.location.hostname.includes("tiktok"))
        imageURL = document.getElementsByTagName("video")[0].parentElement.parentElement.parentElement.parentElement.querySelector("img").src

    return {
        description: description,
        imageURL: imageURL
    }
})();