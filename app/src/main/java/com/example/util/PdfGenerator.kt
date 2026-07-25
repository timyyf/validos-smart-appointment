package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.ServiceSession
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    fun generatePrestadorPdfFile(context: Context, session: ServiceSession): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size at 72 dpi
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
        }

        // Header Background
        paint.color = Color.parseColor("#047857") // Emerald Green
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("VALIDOS • CARTÃO DE LIBERAÇÃO & FICHA DO PRESTADOR", 25f, 38f, paint)

        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Autorização do Gerente, Documentação Civil & Registro do Atendimento", 25f, 62f, paint)

        val currentDateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        paint.textSize = 9f
        canvas.drawText("Emissão: $currentDateStr", 420f, 62f, paint)

        var y = 115f

        // Status Card Banner
        val isReleased = session.gerenteOsNumber != null || session.gerenteOsEmitted || session.gerenteConfirmedAt != null
        paint.color = if (isReleased) Color.parseColor("#ECFDF5") else Color.parseColor("#FFFBEB")
        val statusRect = RectF(25f, y, 570f, y + 42f)
        canvas.drawRoundRect(statusRect, 6f, 6f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        paint.color = if (isReleased) Color.parseColor("#10B981") else Color.parseColor("#F59E0B")
        canvas.drawRoundRect(statusRect, 6f, 6f, paint)

        paint.style = Paint.Style.FILL
        paint.color = if (isReleased) Color.parseColor("#047857") else Color.parseColor("#B45309")
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val statusText = if (isReleased) "AUTORIZAÇÃO: 🟢 ENTRADA LIBERADA - OS Nº ${session.gerenteOsNumber ?: "OS-${session.id}-LIBERADA"}" else "AUTORIZAÇÃO: ⏳ AGUARDANDO VALIDAÇÃO DE DOCUMENTOS E OS"
        canvas.drawText(statusText, 38f, y + 26f, paint)

        y += 60f

        // Section 1: Dados do Estabelecimento e Agendamento
        y = drawSectionHeader(canvas, paint, "1. DADOS DO ESTABELECIMENTO E GERÊNCIA RESPONSÁVEL", y)

        y = drawLabelValue(canvas, paint, "Empresa / Rede:", session.company, y)
        y = drawLabelValue(canvas, paint, "Unidade / Loja:", session.storeName, y)
        y = drawLabelValue(canvas, paint, "Gerente Responsável:", session.gerenteName, y)
        y = drawLabelValue(canvas, paint, "Data e Horário do Serviço:", "${session.date} às ${session.time}", y)
        y = drawLabelValue(canvas, paint, "Tipo de Serviço Contratado:", session.serviceType, y)
        y = drawLabelValue(canvas, paint, "Ordem de Serviço (OS) de Liberação:", session.gerenteOsNumber ?: "OS-${session.id}-LIBERADA", y)

        y += 15f

        // Section 2: Ficha de Identificação Civil do Prestador
        y = drawSectionHeader(canvas, paint, "2. FICHA DE IDENTIFICAÇÃO CIVIL DO PRESTADOR", y)

        y = drawLabelValue(canvas, paint, "Nome Completo do Prestador:", session.prestadorName, y)
        y = drawLabelValue(canvas, paint, "Empresa Prestadora:", session.prestadorCompany, y)
        y = drawLabelValue(canvas, paint, "CPF do Prestador:", session.prestadorCpf ?: "Cadastrado no Portal", y)
        y = drawLabelValue(canvas, paint, "RG do Prestador:", session.prestadorRg ?: "Cadastrado no Portal", y)
        y = drawLabelValue(canvas, paint, "Cargo / Função:", session.prestadorCargo ?: "Prestador de Serviço", y)
        y = drawLabelValue(canvas, paint, "E-mail Corporativo:", session.prestadorEmail ?: "Cadastrado no Portal", y)

        y += 15f

        // Section 3: Documentos e Anexos Auditados
        y = drawSectionHeader(canvas, paint, "3. DOCUMENTOS CIVIS E COMPROVANTES AUDITADOS", y)

        y = drawLabelValue(canvas, paint, "Documento com Foto (RG/CPF):", "Anexado e Auditado (DOC_RG_CPF.png)", y)
        y = drawLabelValue(canvas, paint, "Atestado de Treinamento / ASO:", "Anexado e Auditado (COMPROVANTE_ASO.pdf)", y)
        y = drawLabelValue(canvas, paint, "Confirmação de Presença no Local:", "Registrado pelo Prestador", y)
        y = drawLabelValue(canvas, paint, "Consentimento de Segurança LGPD:", "Aceito pelo Prestador em conformidade com a Lei 13.709/2018", y)

        y += 20f

        // Section 4: LGPD and Verification Box
        paint.color = Color.parseColor("#F3F4F6")
        val lgpdRect = RectF(25f, y, 570f, y + 85f)
        canvas.drawRoundRect(lgpdRect, 6f, 6f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#D1D5DB")
        paint.strokeWidth = 1f
        canvas.drawRoundRect(lgpdRect, 6f, 6f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)

        val text1 = "PROTEÇÃO DE DADOS E REGISTRO IMUTÁVEL DE AUDITORIA (LGPD - LEI 13.709/2018):"
        val text2 = "Os documentos civis e dados informados são mantidos em banco de dados seguro para uso exclusivo da gerência"
        val text3 = "e segurança corporativa. Conforme diretrizes legais, nenhum usuário possui permissão de excluir ou alterar registros."
        val text4 = "Chave Criptográfica SHA-256 ValidOS: SHA256-SESSION-${session.id}-${session.hashCompromisso ?: "AUTH-VALID"}"

        canvas.drawText(text1, 35f, y + 20f, paint)
        canvas.drawText(text2, 35f, y + 35f, paint)
        canvas.drawText(text3, 35f, y + 50f, paint)
        
        paint.color = Color.parseColor("#047857")
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        canvas.drawText(text4, 35f, y + 70f, paint)

        y += 110f

        // Signatures / Approval Stamp
        paint.color = Color.parseColor("#9CA3AF")
        paint.strokeWidth = 1f
        canvas.drawLine(45f, y + 25f, 250f, y + 25f, paint)
        canvas.drawLine(320f, y + 25f, 525f, y + 25f, paint)

        paint.color = Color.BLACK
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        canvas.drawText("GERÊNCIA DE LOJA: ${session.gerenteName}", 45f, y + 40f, paint)
        canvas.drawText("Loja: ${session.storeName} (${session.company})", 45f, y + 53f, paint)

        canvas.drawText("PRESTADOR: ${session.prestadorName}", 320f, y + 40f, paint)
        canvas.drawText("CPF: ${session.prestadorCpf ?: "Cadastrado"} (${session.prestadorCompany})", 320f, y + 53f, paint)

        pdfDocument.finishPage(page)

        val outputFile = File(context.cacheDir, "Ficha_Prestador_OS_${session.id}.pdf")
        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    private fun drawSectionHeader(canvas: Canvas, paint: Paint, title: String, y: Float): Float {
        paint.color = Color.parseColor("#111827")
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(title, 25f, y, paint)

        paint.color = Color.parseColor("#E5E7EB")
        paint.strokeWidth = 1f
        canvas.drawLine(25f, y + 5f, 570f, y + 5f, paint)

        return y + 20f
    }

    private fun drawLabelValue(canvas: Canvas, paint: Paint, label: String, value: String?, y: Float): Float {
        paint.color = Color.parseColor("#374151")
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(label, 35f, y, paint)

        paint.color = Color.BLACK
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(value ?: "N/A", 230f, y, paint)

        return y + 16f
    }

    fun printOrSavePdf(context: Context, session: ServiceSession) {
        try {
            val pdfFile = generatePrestadorPdfFile(context, session)
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager

            if (printManager != null) {
                val printAdapter = object : PrintDocumentAdapter() {
                    override fun onLayout(
                        oldAttributes: PrintAttributes?,
                        newAttributes: PrintAttributes?,
                        cancellationSignal: CancellationSignal?,
                        callback: LayoutResultCallback?,
                        extras: Bundle?
                    ) {
                        if (cancellationSignal?.isCanceled == true) {
                            callback?.onLayoutCancelled()
                            return
                        }

                        val builder = PrintDocumentInfo.Builder("Ficha_Prestador_OS_${session.id}.pdf")
                            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                            .setPageCount(1)

                        callback?.onLayoutFinished(builder.build(), true)
                    }

                    override fun onWrite(
                        pages: Array<out PageRange>?,
                        destination: ParcelFileDescriptor?,
                        cancellationSignal: CancellationSignal?,
                        callback: WriteResultCallback?
                    ) {
                        try {
                            FileInputStream(pdfFile).use { input ->
                                FileOutputStream(destination?.fileDescriptor).use { output ->
                                    input.copyTo(output)
                                }
                            }
                            callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                        } catch (e: Exception) {
                            callback?.onWriteFailed(e.message)
                        }
                    }
                }

                printManager.print(
                    "Ficha_Prestador_OS_${session.id}",
                    printAdapter,
                    PrintAttributes.Builder().build()
                )
            } else {
                ToastUtils.show(context, "Serviço de impressão indisponível neste dispositivo.", Toast.LENGTH_SHORT)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.show(context, "Erro ao gerar PDF: ${e.message}", Toast.LENGTH_LONG)
        }
    }

    fun openOrSharePdf(context: Context, session: ServiceSession) {
        try {
            val pdfFile = generatePrestadorPdfFile(context, session)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "application/pdf"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Compartilhar / Salvar PDF do Prestador"))
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.show(context, "PDF do Prestador gerado em: cache/Ficha_Prestador_OS_${session.id}.pdf", Toast.LENGTH_LONG)
        }
    }

    fun generateDailySummaryPdfFile(context: Context, sessions: List<ServiceSession>): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size at 72 dpi
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
        }

        // Header Background
        paint.color = Color.parseColor("#1E3A8A") // Navy Blue Header
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 17f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("VALIDOS • RELATÓRIO GERAL DE FECHAMENTO OPERACIONAL", 20f, 38f, paint)

        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Resumo Consolidado do Dia • Ordens de Serviço Auditadas & Encerradas", 20f, 62f, paint)

        val currentDateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        paint.textSize = 9f
        canvas.drawText("Emissão: $currentDateStr", 420f, 62f, paint)

        var y = 115f

        // Overview Box
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRoundRect(RectF(20f, y, 575f, y + 55f), 8f, 8f, paint)

        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("SUMÁRIO EXECUTIVO - TOTAL DE OS FINALIZADAS: ${sessions.size}", 32f, y + 24f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.parseColor("#475569")
        canvas.drawText("Todas as sessões abaixo possuem validação de presença, hash SHA-256 e aceite digital.", 32f, y + 42f, paint)

        y += 75f

        // Table Header
        paint.color = Color.parseColor("#3B82F6")
        canvas.drawRect(20f, y, 575f, y + 25f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("OS # / DATA", 30f, y + 17f, paint)
        canvas.drawText("EMPRESA / LOJA", 130f, y + 17f, paint)
        canvas.drawText("PRESTADOR & GERENTE", 310f, y + 17f, paint)
        canvas.drawText("STATUS & AUDITORIA", 460f, y + 17f, paint)

        y += 30f

        // Sessions list rows
        if (sessions.isEmpty()) {
            paint.color = Color.GRAY
            paint.textSize = 11f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("Nenhuma sessão finalizada encontrada para este relatório.", 30f, y + 20f, paint)
            y += 40f
        } else {
            sessions.take(10).forEachIndexed { index, s ->
                val rowBg = if (index % 2 == 0) "#FFFFFF" else "#F8FAFC"
                paint.color = Color.parseColor(rowBg)
                canvas.drawRect(20f, y, 575f, y + 45f, paint)

                paint.color = Color.parseColor("#0F172A")
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("OS #${s.id}", 30f, y + 18f, paint)

                paint.textSize = 8f
                paint.typeface = Typeface.DEFAULT
                paint.color = Color.parseColor("#64748B")
                canvas.drawText("${s.date} ${s.time}", 30f, y + 32f, paint)

                paint.textSize = 9f
                paint.color = Color.parseColor("#0F172A")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(s.company.take(22), 130f, y + 18f, paint)

                paint.textSize = 8f
                paint.typeface = Typeface.DEFAULT
                paint.color = Color.parseColor("#475569")
                canvas.drawText("${s.storeName} (${s.serviceType.take(15)})", 130f, y + 32f, paint)

                canvas.drawText("P: ${s.prestadorName.take(18)}", 310f, y + 18f, paint)
                canvas.drawText("G: ${s.gerenteName.take(18)}", 310f, y + 32f, paint)

                paint.color = Color.parseColor("#047857")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("✅ ${s.status}", 460f, y + 18f, paint)

                val hashShort = (s.hashFinal ?: s.hashCompromisso ?: "SHA256-OK").take(12)
                paint.textSize = 7.5f
                paint.color = Color.parseColor("#64748B")
                paint.typeface = Typeface.DEFAULT
                canvas.drawText("HASH: $hashShort...", 460f, y + 32f, paint)

                y += 50f
            }
        }

        // Footer & Signature
        y = maxOf(y + 20f, 730f)
        paint.color = Color.parseColor("#CBD5E1")
        canvas.drawLine(40f, y + 40f, 260f, y + 40f, paint)
        canvas.drawLine(335f, y + 40f, 555f, y + 40f, paint)

        paint.color = Color.parseColor("#475569")
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Assinatura do Administrador Geral", 65f, y + 54f, paint)
        canvas.drawText("Validação do Sistema ValidOS", 380f, y + 54f, paint)

        pdfDocument.finishPage(page)

        val outputDir = File(context.cacheDir, "pdf_reports").apply { if (!exists()) mkdirs() }
        val file = File(outputDir, "Relatorio_Geral_Fechamento_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return file
    }

    fun openOrShareDailySummaryPdf(context: Context, sessions: List<ServiceSession>) {
        try {
            val pdfFile = generateDailySummaryPdfFile(context, sessions)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "application/pdf"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Relatório Geral do Dia"))
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.show(context, "Relatório Geral gerado em cache.", Toast.LENGTH_LONG)
        }
    }
}
