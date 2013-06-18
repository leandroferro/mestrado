QUALI_NAME = qualificacao
MASTER_NAME = master
BIB_NAME = bibliografia.bib
RESUMO = resumo

LATEX     = latex
PDFLATEX  = pdflatex
BIBTEX    = bibtex
MAKEINDEX = makeindex
PDFVIEWER = evince

pdf: $(MASTER_NAME).pdf

quali: $(QUALI_NAME).pdf

resumo: $(RESUMO).pdf

view: pdf
	$(PDFVIEWER) $(MASTER_NAME).pdf &

view-quali: pdf
	$(PDFVIEWER) $(QUALI_NAME).pdf &
		
view-resumo: $(RESUMO).pdf
	$(PDFVIEWER) $(RESUMO).pdf &

$(MASTER_NAME).pdf: $(MASTER_NAME).tex $(BIB_NAME)
	$(PDFLATEX) $<
	$(BIBTEX) $(MASTER_NAME) 
	$(MAKEINDEX) $(MASTER_NAME) 
	$(PDFLATEX) $< 
	$(PDFLATEX) $<
	$(PDFLATEX) $<

$(QUALI_NAME).pdf: $(QUALI_NAME).tex $(BIB_NAME)
	$(PDFLATEX) $<
	$(BIBTEX) $(QUALI_NAME) 
	$(MAKEINDEX) $(QUALI_NAME) 
	$(PDFLATEX) $< 
	$(PDFLATEX) $<
	$(PDFLATEX) $<

$(RESUMO).pdf: $(RESUMO).tex 
	$(PDFLATEX) $<

clean:
	rm -f $(MASTER_NAME)*.ps $(MASTER_NAME)*.dvi $(MASTER_NAME)*.bbl $(MASTER_NAME)*.pdf $(MASTER_NAME)*.out \
		  $(MASTER_NAME)*.lof $(MASTER_NAME)*.lot \
		  $(QUALI_NAME)*.ps $(QUALI_NAME)*.dvi $(QUALI_NAME)*.bbl $(QUALI_NAME)*.pdf $(QUALI_NAME)*.out \
		  $(QUALI_NAME)*.lof $(QUALI_NAME)*.lot \
		  $(RESUMO)*.ps $(RESUMO)*.dvi $(RESUMO)*.bbl $(RESUMO)*.pdf $(RESUMO)*.out \
		  $(RESUMO)*.lof $(RESUMO)*.lot \
		  *.log *.aux *.blg *.toc *.brf *.ilg *.ind

imagens:
	for i in `ls imagens/*.eps`
	do
		epstopdf $i
	done
