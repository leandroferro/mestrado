BASE_NAME = qualificacao
BIB_NAME = bibliografia.bib
RESUMO = resumo

LATEX     = latex
PDFLATEX  = pdflatex
BIBTEX    = bibtex
MAKEINDEX = makeindex
PDFVIEWER = evince

pdf: $(BASE_NAME).pdf

resumo: $(RESUMO).pdf

view: pdf
	$(PDFVIEWER) $(BASE_NAME).pdf &
	
view-resumo: $(RESUMO).pdf
	$(PDFVIEWER) $(RESUMO).pdf &

$(BASE_NAME).pdf: $(BASE_NAME).tex $(BIB_NAME)
	$(PDFLATEX) $<
	$(BIBTEX) $(BASE_NAME) 
	$(MAKEINDEX) $(BASE_NAME) 
	$(PDFLATEX) $< 
	$(PDFLATEX) $<
	$(PDFLATEX) $<

$(RESUMO).pdf: $(RESUMO).tex 
	$(PDFLATEX) $<

clean:
	rm -f $(BASE_NAME)*.ps $(BASE_NAME)*.dvi *.log \
	      *.aux *.blg *.toc *.brf *.ilg *.ind \
	      missfont.log $(BASE_NAME)*.bbl $(BASE_NAME)*.pdf $(BASE_NAME)*.out \
		  $(BASE_NAME)*.lof $(BASE_NAME)*.lot $(RESUMO)*.ps $(RESUMO)*.dvi \
		  $(RESUMO)*.bbl $(RESUMO)*.pdf $(RESUMO)*.out $(RESUMO)*.lof $(RESUMO)*.lot

imagens:
	for i in `ls imagens/*.eps`
	do
		epstopdf $i
	done
