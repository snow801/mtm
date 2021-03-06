package com.wj.tmall.service.impl;

import com.wj.tmall.mapper.ProductMapper;
import com.wj.tmall.pojo.Category;
import com.wj.tmall.pojo.Product;
import com.wj.tmall.pojo.ProductExample;
import com.wj.tmall.pojo.ProductImage;


import com.wj.tmall.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class ProductServiceImpl implements ProductService {
        @Autowired
        ProductMapper productMapper;
        @Autowired
        CategoryService categoryService;
        @Autowired
        ProductImageService productImageService;
        @Autowired
        OrderItemService orderItemService;
        @Autowired
        ReviewService reviewService;

        @Override
        public  void add(Product p){
            productMapper.insert(p);
        }

        @Override
        public  void delete(int id){
            productMapper.deleteByPrimaryKey(id);
        }

        @Override
        public void update(Product p){
            productMapper.updateByPrimaryKey(p);
        }

        @Override
        public Product get(int id){
            Product p=productMapper.selectByPrimaryKey(id);
            setFirstProductImage(p);        //订单管理图片显示
            setCategory(p);
            return  p;
        }
        public  void setCategory(List<Product> ps){
            for(Product p:ps)
            setCategory(p);
        }
        public void setCategory(Product p){
                    int cid=p.getCid();
                    Category c=categoryService.get(cid);
                    p.setCategory(c);
        }

        @Override
        public  List list(int cid){
            ProductExample example=new ProductExample();
            example.createCriteria().andCidEqualTo(cid);
            example.setOrderByClause("id asc");
            List result=productMapper.selectByExample(example);
            setCategory(result);
            setFirstProductImage(result);
            return  result;
        }

        @Override
        public  void   setFirstProductImage(Product p){
            List<ProductImage> pis = productImageService.list(p.getId(), ProductImageService.type_single);
            if (!pis.isEmpty()) {
                ProductImage pi = pis.get(0);
                p.setFirstProductImage(pi);
            }
        }

        //为分类填充产品集合
        @Override
        public void fill(List<Category> cs) {
            for (Category c : cs) {
                fill(c);
            }
        }

        //为多个分类填充产品集合
        @Override
        public void fill(Category c) {
            List<Product> ps = list(c.getId());
            c.setProducts(ps);
        }

        //分类下的产品集合，按照8个为一行，拆成多行，利于后续页面上进行显示
        @Override
        public void fillByRow(List<Category> cs) {
            int productNumberEachRow = 8;
            for (Category c : cs) {
                List<Product> products =  c.getProducts();
                List<List<Product>> productsByRow =  new ArrayList<>();
                for (int i = 0; i < products.size(); i+=productNumberEachRow) {
                    int size = i+productNumberEachRow;
                    size= size>products.size()?products.size():size;
                    List<Product> productsOfEachRow =products.subList(i, size);
                    productsByRow.add(productsOfEachRow);
                }
                c.setProductsByRow(productsByRow);
            }
        }

        public void setFirstProductImage(List<Product> ps) {
        for (Product p : ps) {
            setFirstProductImage(p);
            }
         }

        @Override
        public void setSaleAndReviewNumber(Product p) {
            int saleCount = orderItemService.getSaleCount(p.getId());
            p.setSaleCount(saleCount);

            int reviewCount = reviewService.getCount(p.getId());
            p.setReviewCount(reviewCount);
        }

        @Override
        public void setSaleAndReviewNumber(List<Product> ps) {
            for (Product p : ps) {
                setSaleAndReviewNumber(p);
            }
        }

        //通过keyword关键字模糊查询
        @Override
        public List<Product> search(String keyword) {
            ProductExample example = new ProductExample();
            example.createCriteria().andNameLike("%" + keyword + "%");
            example.setOrderByClause("id desc");
            List result = productMapper.selectByExample(example);
            setFirstProductImage(result);
            setCategory(result);
            return result;
        }

}
