import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { BasicInfoComponent } from './basic-info/basic-info.component';
import { SearhAreaComponent } from './searh-area/searh-area.component';
import { WatchlistComponent } from './watchlist/watchlist.component';
import { PortfolioComponent } from './portfolio/portfolio.component';


const routes: Routes = [
    {   path: '', 
        redirectTo: '/search/home',  
        pathMatch: 'full'
    },
    {
        path: 'search/:ticker',
        component: SearhAreaComponent
    },
    {
        path: 'watchlist',
        component: WatchlistComponent
    },
    {
        path: 'portfolio',
        component: PortfolioComponent
    }
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})

export class AppRoutingModule {}

export const routingComponents = [BasicInfoComponent, SearhAreaComponent, PortfolioComponent] 