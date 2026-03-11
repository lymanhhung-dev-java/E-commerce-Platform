import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface Ward {
    code: string;
    name: string;
    fullName: string;
    slug: string;
    type: string;
}

export interface Province {
    code: string;
    name: string;
    slug: string;
    type: string;
    isCentral: boolean;
    fullName: string;
    wards: Ward[];
}

@Injectable({
    providedIn: 'root'
})
export class LocationService {
    private http = inject(HttpClient);
    private dataUrl = '/data/tree.json';

    getProvinces(): Observable<Province[]> {
        return this.http.get<Province[]>(this.dataUrl);
    }

    getWardsByProvinceName(provinceName: string): Observable<Ward[]> {
        return this.getProvinces().pipe(
            map(provinces => {
                const province = provinces.find(p => p.name === provinceName);
                return province ? province.wards : [];
            })
        );
    }
}
