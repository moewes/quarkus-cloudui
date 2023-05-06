import { LitElement, html, css} from 'lit';
import { JsonRpc } from 'jsonrpc';
import '@vaadin/icon';
import '@vaadin/button';
import { until } from 'lit/directives/until.js';
import '@vaadin/grid';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import '@vaadin/grid/vaadin-grid-sort-column.js';
import { unsafeHTML } from 'lit-html/directives/unsafe-html.js';

export class CloudUiViewsComponent extends LitElement {

    jsonRpc = new JsonRpc("CloudUi");

    static properties = {
        "_views": {state: true, type: Array}
    }

    connectedCallback() {
        super.connectedCallback();
        console.log("connected callback")
        this.jsonRpc.getInfo().then(jsonRpcResponse => {
            this._views = [];
            jsonRpcResponse.result.forEach(c => {
                this._views.push(c);
            });
        });
    }


    render() {
        return html`${until(this._renderChannelTable(), html`<span>Loading channels...</span>`)}`;
    }

    _renderChannelTable() {
        if (this._views) {
            return html`
                <vaadin-grid .items="${this._views}" class="datatable" theme="no-border">
                    <vaadin-grid-column auto-width
                                        header="View"
                                        ${columnBodyRenderer(this._viewNameRenderer, [])}>
                    </vaadin-grid-column>

                    <vaadin-grid-column auto-width
                                        header="Path"
                                        ${columnBodyRenderer(this._viewPathRenderer, [])}>>
                    </vaadin-grid-column>
                </vaadin-grid>`;
        }
    }

    _viewNameRenderer(view) {
        return html`<strong>${ view.view }</strong>`
    }

    _viewPathRenderer(view) {
            return html`${ view.path }`
        }
}

customElements.define('dev-ui-views', CloudUiViewsComponent);