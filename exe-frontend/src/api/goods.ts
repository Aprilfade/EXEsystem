import request from '@/utils/request';
import type { ApiResult } from './user';

export interface Goods {
    id: number;
    name: string;
    description: string;
    price: number;
    type: string; // AVATAR_FRAME
    resourceValue: string;
    isOwned: boolean; // 后端计算返回
}

// 获取商品列表
export function fetchGoodsList(): Promise<ApiResult<Goods[]>> {
    return request({
        url: '/api/v1/student/mall/list',
        method: 'get'
    });
}

// 兑换商品
export function exchangeGoods(goodsId: number): Promise<ApiResult<any>> {
    return request({
        url: `/api/v1/student/mall/exchange/${goodsId}`,
        method: 'post'
    });
}
// 【新增】装配商品
export function equipGoods(goodsId: number): Promise<ApiResult<any>> {
    return request({
        url: `/api/v1/student/mall/equip/${goodsId}`,
        method: 'post'
    });
}