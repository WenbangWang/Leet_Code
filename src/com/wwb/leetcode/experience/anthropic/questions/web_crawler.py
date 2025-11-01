from typing import Self
import requests
import asyncio
import time
from bs4 import BeautifulSoup
from urllib.parse import urljoin, urlparse

'''
def getLinksFrom(link: str) -> list[str]:
    try:
      resp = requests.get(link, timeout=5)
      resp.raise_for_status()
    except requests.RequestException as e:
      print(f"Error fetching {link}:{e}")
      return []

    soup = BeautifulSoup(resp.text, "html.parser")
    links = []
    for a in soup.find_all("a", href=True):
      abs_url = urljoin(link, a["href"])
      links.append(abs_url)
    return links

class Solution:
    def __init__(self, max_concurrency=10):
      self.visited = set()
      self.semaphore = asyncio.Semaphore(max_concurrency)
      self.start_hostname = None

    def sanitize(self, url):
      idx = url.find('#')
      if idx > 0:
          url = url[:idx]
      return url

    def get_hostname(self, url):
      return urlparse(url).hostname

    async def dfs(self, url):
      url = self.sanitize(url)
      if url in self.visited or self.get_hostname(url) != self.start_hostname:
          return
      self.visited.add(url)
      async with self.semaphore:

          next_urls = await asyncio.to_thread(getLinksFrom, url)
      await asyncio.gather(*(self.dfs(u) for u in next_urls))

    async def crawl(self, start_url):
      self.start_hostname = self.get_hostname(start_url)
      await self.dfs(start_url)
      return list(self.visited)

async def main():
    start = time.time()
    solution = Solution(max_concurrency = 10)
    start_url = "https://andyljones.com"
    result = await solution.crawl(start_url)

    print("Time taken:", time.time() - start)

    print(f"\nCrawled {len(result)} URLs: \n")
    for url in sorted(result):
      print(url)

if __name__ == "__main__":
    asyncio.run(main())
'''

import asyncio
import aiohttp
from bs4 import BeautifulSoup
from urllib.parse import urljoin, urlparse


class AsyncCrawler:
    def __init__(self, max_concurrency=10):
        self.visited = set()
        self.semaphore = asyncio.Semaphore(max_concurrency)
        self.start_hostname = None
        self.session = None

    def get_hostname(self, url):
        return urlparse(url).hostname

    def sanitize(self, url):
        idx = url.find('#')
        if idx > 0:
            url = url[:idx]
        return url

    async def get_links_from(self, url):
        try:
            async with self.semaphore:
                async with self.session.get(url, timeout=10) as resp:
                    print(url)
                    if resp.status != 200:
                        return []
                    text = await resp.text()
                    soup = BeautifulSoup(text, "html.parser")
                    return [urljoin(url, a["href"])
                            for a in soup.find_all("a", href=True)]
        except Exception as e:
            print("Error fetching {url}: {e}")
            return []

    async def crawl(self, start_url):
        self.start_hostname = self.get_hostname(start_url)
        self.session = aiohttp.ClientSession()
        await self.dfs(start_url)
        await self.session.close()
        return list(self.visited)

    async def dfs(self, url):
        url = self.sanitize(url)
        if url in self.visited or self.get_hostname(url) != self.start_hostname:
            return
        self.visited.add(url)
        links = await self.get_links_from(url)
        # Crawl links concurrently
        await asyncio.gather(*(self.dfs(link) for link in links))


def main():
    start_url = "https://andyljones.com"
    crawler = AsyncCrawler()
    result = asyncio.run(crawler.crawl(start_url))
    print(f"\nCrawled {len(result)} URLs:\n")
    for url in sorted(result):
        print(url)


if __name__ == "__main__":
    main()
