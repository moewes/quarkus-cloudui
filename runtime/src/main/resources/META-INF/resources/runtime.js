function handleEvents(event) {

     switch (event.detail.type) {
        case 'replaceState' :
            history.replaceState(null,"",event.detail.url);
            break;
        case 'setTitle' :
            document.title = event.detail.title;
            break;
        case 'showAlert' :
            alert(event.detail.text);
            break;
        case 'open' :
            window.open(event.detail.url,event.detail.name);
            break;
        default:
            console.log('Event-type ' + event.detail.type + 'not supported');
     }
}