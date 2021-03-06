import {NgModule} from '@angular/core';
import {ApplicationDashboardComponent} from './application-dashboard.component';
import {ApplicationSelectComponent} from './components/application-select/application-select.component';
import {PageComponent} from './components/page/page.component';
import {RouterModule, Routes} from "@angular/router";
import {CsiGraphComponent} from './components/csi-graph/csi-graph.component';
import {PageMetricComponent} from './components/page-metric/page-metric.component';
import {CsiInfoComponent} from "./components/csi-info/csi-info.component";
import {SharedModule} from "../shared/shared.module";
import {HttpClientModule} from "@angular/common/http";
import { ApplicationJobStatusComponent } from './components/application-job-status/application-job-status.component';
import {GraphiteIntegrationComponent} from "./components/application-job-status/graphite-integration/graphite-integration.component";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import { PerformanceAspectManagementComponent } from './components/performance-aspect-management/performance-aspect-management.component';
import {ResultSelectionModule} from "../result-selection/result-selection.module";
import { PerformanceAspectInspectComponent } from './components/performance-aspect-management/performance-aspect-inspect/performance-aspect-inspect.component';

const DashboardRoutes: Routes = [
  {path: '', component: ApplicationDashboardComponent},
  {path: ':applicationId', component: ApplicationDashboardComponent}
];

@NgModule({
  imports: [
    RouterModule.forChild(DashboardRoutes),
    SharedModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule,
    ResultSelectionModule
  ],
  declarations: [
    ApplicationDashboardComponent,
    ApplicationSelectComponent,
    PageComponent,
    CsiGraphComponent,
    CsiInfoComponent,
    PageMetricComponent,
    ApplicationJobStatusComponent,
    GraphiteIntegrationComponent,
    PerformanceAspectManagementComponent,
    PerformanceAspectInspectComponent,
  ],
  exports: [
    RouterModule
  ]
})
export class ApplicationDashboardModule {
}
