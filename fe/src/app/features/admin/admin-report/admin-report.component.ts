import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminReportService, ReportResponse } from '../../../core/services/admin-report.service';
import Swal from 'sweetalert2';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-admin-report',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-report.component.html'
})
export class AdminReportComponent implements OnInit {
  private reportService = inject(AdminReportService);
  private toastr = inject(ToastrService);

  reports: ReportResponse[] = [];
  isLoading = false;

  ngOnInit() {
    this.loadReports();
  }

  loadReports() {
    this.isLoading = true;
    this.reportService.getAllReports().subscribe({
      next: (res: ReportResponse[]) => {
        this.reports = res;
        this.isLoading = false;
      },
      error: () => {
        this.toastr.error('Lỗi tải danh sách khiếu nại');
        this.isLoading = false;
      }
    });
  }

  resolveReport(id: number, currentStatus: string) {
    if (currentStatus !== 'PENDING') return;

    Swal.fire({
      title: 'Xử lý Khiếu nại',
      html: `
        <select id="swal-resolution" class="swal2-select">
          <option value="" disabled selected>Chọn kết quả...</option>
          <option value="RESOLVED">Chấp nhận & Đền bù (RESOLVED)</option>
          <option value="REJECTED">Bác bỏ (REJECTED)</option>
        </select>
        <textarea id="swal-note" class="swal2-textarea" placeholder="Nhập ghi chú xử lý của bạn..."></textarea>
      `,
      showCancelButton: true,
      confirmButtonText: 'Lưu kết quả',
      cancelButtonText: 'Hủy',
      preConfirm: () => {
        const resolution = (document.getElementById('swal-resolution') as HTMLSelectElement).value;
        const note = (document.getElementById('swal-note') as HTMLTextAreaElement).value;
        
        if (!resolution) {
          Swal.showValidationMessage('Vui lòng chọn kết quả giải quyết');
          return false;
        }
        return { resolution, note };
      }
    }).then((result) => {
      if (result.isConfirmed && result.value) {
        const val: any = result.value;
        this.reportService.resolveReport(id, val.resolution as any, val.note).subscribe({
          next: () => {
             this.toastr.success('Đã xử lý khiếu nại thành công');
             this.loadReports();
          },
          error: () => this.toastr.error('Lỗi khi xử lý')
        });
      }
    });
  }
}
