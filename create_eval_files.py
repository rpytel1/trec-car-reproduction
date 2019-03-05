from trec_car.format_runs import *
from trec_car.read_data import *


def retrieve_paragraph(file):
    para_dict = {}
    for p in iter_paragraphs(open(file, 'rb')):
        para_dict[str(p.para_id)] = p
    return para_dict


with open('test200_first_results5.txt') as fp:
    lines = fp.readlines()
content = [x.split() for x in lines]
file = r'test200-train/train.pages.cbor-paragraphs.cbor'
my_ranking = []
i = 0
d = retrieve_paragraph(file)
for c in content:
    p = d[c[2]]
    s = float(c[4].replace(',','.'))
    r = int(c[3])
    my_ranking.append((p,s,r))
    print(i)
    i+=1
print('out')
file = r'test200-train/train.pages.cbor'
start = 0
with open('runfile', mode='w', encoding='UTF-8') as writer:
    for page in iter_annotations(open(file, 'rb')):
        query_id = str(page.page_id)
        ranking = [RankingEntry(query_id, p.para_id, r, s, paragraph_content=p) for p, s, r in my_ranking[start:start + 5]]
        start += 5
        format_run(writer, ranking, exp_name='test')
        for section_path in page.flat_headings_list():
            query_id = "/".join([page.page_id] + [section.headingId for section in section_path])
            ranking = [RankingEntry(query_id, p.para_id, r, s, paragraph_content=p) for p, s, r in my_ranking[start:start + 5]]
            start += 5
            format_run(writer, ranking, exp_name='test')


