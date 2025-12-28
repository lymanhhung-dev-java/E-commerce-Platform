import { Component, inject, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { UserService } from "../../../core/services/user.service";
import { ToastrService } from "ngx-toastr";

@Component({
    selector: "app-user-list",
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: "./user-list.html",
    styleUrl: "./user-list.css",
})
export class UserListComponent implements OnInit {
    userService = inject(UserService);
    toastr = inject(ToastrService);

    users: any[] = [];

    currentPage: number = 0;
    pageSize: number = 10;
    totalPages: number = 0;
    totalElements: number = 0;

    keyword: string = '';
    selectedStatus: string = 'ALL';
    selectedRole: string = 'ALL';

    ngOnInit(): void {
        this.loadUsers();
    }

    loadUsers() {
        let isShopOwnerParam: boolean | undefined = undefined;
        if (this.selectedRole === 'SHOP_OWNER') isShopOwnerParam = true;
        if (this.selectedRole === 'USER_ONLY') isShopOwnerParam = false;

        this.userService.getAllUsers(this.currentPage, this.pageSize, this.keyword, this.selectedStatus, isShopOwnerParam)
            .subscribe({
                next: (res) => {
                    this.users = res.content;
                    this.totalPages = res.totalPages;
                    this.totalElements = res.totalElements;
                },
                error: () => this.toastr.error('Lỗi tải danh sách users')
            });
    }
    onSearch() {
        this.currentPage = 0;
        this.loadUsers();
    }

    onPageChange(page: number) {
        this.currentPage = page;
        this.loadUsers();
    }

    toggleStatus(user: any) {

        const newStatus = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';

        const actionName = user.status === 'ACTIVE' ? 'KHÓA' : 'MỞ KHÓA';

        if (confirm(`Bạn có chắc chắn muốn ${actionName} tài khoản "${user.username}"?`)) {

            this.userService.updateUserStatus(user.id, newStatus).subscribe({
                next: (msg) => {
                    this.toastr.success(msg || 'Cập nhật thành công');

                    user.status = newStatus;
                },
                error: (err) => {
                    console.error(err);
                    this.toastr.error('Không thể cập nhật trạng thái');
                }
            });
        }
    }
}
