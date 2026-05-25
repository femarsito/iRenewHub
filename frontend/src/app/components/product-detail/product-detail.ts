import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Product } from '../../models/product.model';
import { ProductService } from '../../services/product';
import { CartService } from '../../services/cart';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-product-detail',
  standalone: false,
  templateUrl: './product-detail.html',
  styleUrl: './product-detail.css'
})
export class ProductDetail implements OnInit {

  readonly API = 'http://localhost:8080/api';

  product: Product | undefined;
  isLoading = false;
  errorMessage = '';
  addedToCart = false;

  // ---- COMENTARIOS ----
  comments: any[] = [];
  canComment = false;          // true si el usuario compró el producto
  newCommentText = '';
  commentError = '';
  commentSuccess = '';

  // Responder: guarda el id del comentario al que se responde
  replyingTo: number | null = null;
  replyText = '';

  // Denunciar: guarda el id del comentario que se denuncia
  reportingId: number | null = null;
  reportReason = '';
  reportError = '';
  reportSuccess = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private cartService: CartService,
    private http: HttpClient,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    // Suscripción al paramMap en lugar de snapshot para que se recargue
    // aunque el usuario vuelva con el botón Atrás del navegador
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));
      this.loadProductDetail(id);
    });
  }

  loadProductDetail(id: number): void {
    this.isLoading = true;
    if (!id) { this.errorMessage = 'ID de producto no válido'; this.isLoading = false; return; }

    this.productService.getProductById(id).subscribe({
      next: (data: Product | undefined) => {
        if (data) {
          this.product = data;
          this.loadComments(id);
          this.checkCanComment(id);
        } else {
          this.errorMessage = 'Producto no encontrado';
        }
        this.isLoading = false;
      },
      error: () => { this.errorMessage = 'Error al cargar el producto'; this.isLoading = false; }
    });
  }

  // ---- CARGAR COMENTARIOS ----
  loadComments(productId: number): void {
    this.http.get<any[]>(`${this.API}/products/${productId}/comments`).subscribe({
      next: (data) => { this.comments = data ?? []; },
      error: (err) => { console.error('Error cargando comentarios:', err); this.comments = []; }
    });
  }

  checkCanComment(productId: number): void {
    this.http.get<any>(`${this.API}/products/${productId}/can-comment`).subscribe({
      next: (res) => { this.canComment = res.canComment; },
      error: () => { this.canComment = false; }
    });
  }

  // ---- AÑADIR COMENTARIO RAÍZ ----
  submitComment(): void {
    if (!this.newCommentText.trim()) return;
    this.commentError = '';
    this.http.post<any>(`${this.API}/products/${this.product!.id}/comments`,
      { content: this.newCommentText, parentId: null }
    ).subscribe({
      next: (c) => {
        this.comments.unshift(c);
        this.newCommentText = '';
        this.commentSuccess = '¡Comentario publicado!';
        setTimeout(() => this.commentSuccess = '', 3000);
      },
      error: (err) => {
        this.commentError = err.error?.message || 'Error al publicar el comentario.';
      }
    });
  }

  // ---- RESPONDER ----
  startReply(commentId: number): void {
    this.replyingTo = this.replyingTo === commentId ? null : commentId;
    this.replyText = '';
  }

  submitReply(parentComment: any): void {
    if (!this.replyText.trim()) return;
    this.http.post<any>(`${this.API}/products/${this.product!.id}/comments`,
      { content: this.replyText, parentId: parentComment.id }
    ).subscribe({
      next: (reply) => {
        if (!parentComment.replies) parentComment.replies = [];
        parentComment.replies.push(reply);
        this.replyingTo = null;
        this.replyText = '';
      },
      error: () => {}
    });
  }

  // ---- LIKE ----
  toggleLike(comment: any): void {
    this.http.post<any>(`${this.API}/comments/${comment.id}/like`, {}).subscribe({
      next: (res) => {
        comment.likes = res.likes;
        comment.hasLiked = res.hasLiked;
      },
      error: () => {}
    });
  }

  // ---- DENUNCIAR ----
  startReport(commentId: number): void {
    this.reportingId = this.reportingId === commentId ? null : commentId;
    this.reportReason = '';
    this.reportError = '';
    this.reportSuccess = '';
  }

  submitReport(commentId: number): void {
    if (!this.reportReason.trim()) return;
    this.http.post<any>(`${this.API}/comments/${commentId}/report`,
      { reason: this.reportReason }
    ).subscribe({
      next: () => {
        this.reportSuccess = 'Denuncia enviada al administrador.';
        this.reportingId = null;
        this.reportReason = '';
        setTimeout(() => this.reportSuccess = '', 4000);
      },
      error: (err) => {
        this.reportError = err.error?.message || 'Error al enviar la denuncia.';
      }
    });
  }

  // ---- CARRITO ----
  goBack(): void { this.router.navigate(['/products']); }

  addToCart(): void {
    if (this.product) {
      const current = this.cartService.getCartItems().find(i => i.product.id === this.product?.id);
      if ((current?.quantity ?? 0) >= this.product.stock) {
        alert(`❌ Ya tienes el máximo disponible (${this.product.stock} unidades)`);
        return;
      }
      this.cartService.addToCart(this.product);
      this.addedToCart = true;
      setTimeout(() => this.addedToCart = false, 2000);
    }
  }
}
